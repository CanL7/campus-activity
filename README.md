# campus-activity-api

校园活动报名系统后端。围绕「活动发布 → 学生报名 → 支付 → 超时释放」这条链路，
实现了一套带并发控制和权限体系的报名 API。

主要想解决的问题是**报名这一步的并发正确性**：名额是有限的，多个请求同时抢的时候既不能超卖，
也不能出现「同一个人占了两个名额」。

## 技术栈

| 组件 | 版本 | 备注 |
|---|---|---|
| Java | 17 | |
| Spring Boot | 4.1.0 | |
| MyBatis | 3.5.19 | `mybatis-spring-boot-starter` 4.0.1 |
| MySQL | 8.0 | |
| PageHelper | 6.1.1 | 用核心包手动注册，**非 starter**（starter 的自动配置不兼容 Spring Boot 4） |
| jjwt | 0.13.0 | 拆成 api / impl / jackson 三个 artifact |
| spring-security-crypto | 7.1.0 | 只取 BCrypt 做密码哈希，不引入整套 Spring Security |
| spring-boot-starter-aspectj | 4.1.0 | AOP。Spring Boot 4 里由 `spring-boot-starter-aop` 改名而来 |

## 接口一览

认证方式：`Authorization: Bearer <token>`。未标注「公开」的接口都需要登录。

### 认证 `/auth`（公开）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/auth/register` | 注册 |
| POST | `/auth/login` | 登录，返回 token |

### 活动 `/activities`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/activities` | 公开 | 分页查询，支持 `keyword` / `status` 过滤 |
| GET | `/activities/{id}` | 公开 | 查询单个活动 |
| POST | `/activities` | 管理员 | 新增活动 |
| PUT | `/activities/{id}` | 管理员 | 修改活动 |
| DELETE | `/activities/{id}` | 管理员 | 删除活动 |

### 报名 `/enrollments`（需登录）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/enrollments` | 报名，返回报名记录 id |
| PUT | `/enrollments/{id}` | 取消报名（软删除，`status` → 2） |
| PUT | `/enrollments/{id}/pay` | 支付 |
| GET | `/enrollments/my` | 分页查询我的报名 |

### 用户 `/users`（需登录）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/users` | 新增用户 |
| GET | `/users/{id}` | 查询用户 |
| PUT | `/users/{id}` | 修改用户 |
| DELETE | `/users/{id}` | 删除用户 |

## 数据模型

三张表：`activity` / `activity_enrollment` / `users`。建表脚本见 [`sql/schema.sql`](sql/schema.sql)。

状态枚举：

- `activity.status`：`0` 未发布 / `1` 已发布 / `2` 已取消
- `activity_enrollment.status`：`0` 待支付 / `1` 已确认 / `2` 已取消或已作废
- `users.role`：`0` 普通用户 / `1` 管理员

`activity_enrollment` 上有一个生成列，下面第 2 点会用到：

```sql
active_flag bigint GENERATED ALWAYS AS (if(status in (0,1), user_id, null)) STORED
```

## 几个关键设计

### 1. 并发报名防超卖

不能写成「先 `SELECT` 查名额，再 `UPDATE` 扣减」—— 这两步之间的空隙就是 bug 的入口，
并发下多个请求会读到同一个旧值，然后一起扣减。

改成让数据库在**一次原子操作**里完成「检查 + 扣减」：

```sql
UPDATE activity
SET enrolled_count = enrolled_count + 1
WHERE id = #{activityId} AND capacity > enrolled_count
```

影响行数为 0 就是没抢到，直接抛「报名人数已满」。判断和修改在同一行的锁内完成，不存在中间态。

### 2. 取消之后可以重新报名

难点在于：同一个人对同一个活动只能有一条**有效**报名，但取消之后应该能再报一次。

做法是生成列 + 唯一索引。`active_flag` 在报名有效时等于 `user_id`，取消/作废后变成 `NULL`。
唯一索引里 **`NULL` 可以无限重复**（`NULL = NULL` 的结果是 `UNKNOWN` 而不是 `TRUE`），
所以多条「已取消」记录不会互相冲突，而「有效」记录仍然唯一。

业务代码对这个机制零感知 —— 它只会在插入重复有效报名时收到一个 `DuplicateKeyException`。

### 3. 15 分钟未支付自动释放名额

定时任务每 30 秒扫描一次 `status = 0 AND expire_time < now()` 的报名，批量置为作废并批量归还名额。

扫描和执行**都带 `status = 0` 条件** —— 因为「查出过期记录」和「处理它」之间有时间差，
这期间用户可能刚好完成支付。

### 4. 支付兜住定时任务的窗口

```sql
UPDATE activity_enrollment
SET status = 1
WHERE id = #{id}
  AND status = 0            -- 防重复支付
  AND expire_time > now()   -- 补上定时任务两轮扫描之间的间隙
  AND user_id = #{userId}   -- 防越权
```

三个守卫缺一不可。影响行数为 0 说明报名不存在、状态不对、已过期、或者不是你的。

### 5. 认证与授权

- **JWT 无状态**：`userId` 和 `role` 都放在 payload 里，拦截器验签即得身份，**不查库**。
  代价是改角色后要等 token 过期（30 分钟）才生效。
- **授权用自定义注解**：`@RequireAdmin` 声明「这方法要管理员」，`@PublicApi` 声明「可以匿名访问」，
  由 `JwtInterceptor` 统一读取判定。相比在配置里维护 URL 白名单，注解和接口写在同一行，
  新增接口不会漏配。
- **401 ≠ 403**：401 是「不知道你是谁」（重新登录有用），403 是「知道你是谁但没权限」（重新登录没用）。

### 6. 水平越权（IDOR）防护

路径里的 id 只负责**定位资源**，而「这条资源是不是你的」一律由 token 里的 `userId` 判定：

```sql
UPDATE activity_enrollment
SET status = 2
WHERE id = #{id} AND user_id = #{userId}
```

影响行数为 0 时返回 **404 而不是 403** —— 403 等于告诉攻击者「这个 id 存在，只是不属于你」。

## 本地运行

1. 建库建表：

```bash
mysql -u root -p -e "CREATE DATABASE campus_db DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p campus_db < sql/schema.sql
```

2. 建一个只授权 `campus_db` 的应用账号（不建议直接用 root 跑应用）：

```sql
CREATE USER 'campus_app'@'127.0.0.1' IDENTIFIED BY '<你的密码>';
GRANT ALL PRIVILEGES ON campus_db.* TO 'campus_app'@'127.0.0.1';
```

3. 按实际连接信息改 `src/main/resources/application.yaml`
4. 启动：直接运行 `CampusActivityApiApplication`，或

```bash
mvn spring-boot:run
```

JWT 密钥通过环境变量注入，未设置时使用配置文件里的开发默认值：

```yaml
jwt:
  secret: ${JWT_SECRET:campus-activity-dev-secret-key-at-least-32-bytes}
```

密钥长度决定签名算法（jjwt 会按字节数自动选择）：32 字节 → HS256，48 字节 → HS384，64 字节 → HS512。
上面这个默认值 48 字节，签出来的是 HS384。少于 32 字节会直接抛 `WeakKeyException` 启动失败。

## 接口约定

**所有接口的 HTTP 状态码恒为 200，业务状态码在响应体的 `code` 字段里。**

```json
{ "code": 200, "msg": "success", "data": {} }
{ "code": 404, "msg": "活动不存在", "data": null }
```

这不是疏忽 —— 全局异常处理统一返回 `Result<T>` 而不是 `ResponseEntity`。
调用方判断成功与否**必须读 body 里的 `code`**，只看 HTTP 状态码会误判。

参数校验失败时 `data` 里是字段级错误列表。
