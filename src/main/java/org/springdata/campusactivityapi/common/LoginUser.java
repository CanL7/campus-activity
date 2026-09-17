package org.springdata.campusactivityapi.common;

/**
 * "当前登录的人"。只存两个字段：身份 + 角色。
 *
 * 注意这跟 entity/User 不是一回事：
 *   User      —— 数据库里的一行（含密码密文等敏感字段），只在 Service/Mapper 层流转
 *   LoginUser —— 从 token 里解出来的最小信息集，用于"服务端判断是谁、能不能干这事"
 *
 * 为什么不让 CurrentUser 继续只存 Long userId：
 *   权限判断要用 role，如果只存 id，拦截器每次都得去查库拿角色 —— 那 token 里带 role 就白带了。
 *   直接从 token 解出来存着，零数据库开销。
 *
 * 用 record 而不是普通类：不可变（final 字段 + 无 setter），
 * 存进 ThreadLocal 后不会被别处偷偷改掉 —— 权限对象尤其怕被改。
 */
public record LoginUser(Long userId, Integer role) {

    /** 角色常量。写成常量而不是到处硬编码 1，改的时候只改一处 */
    public static final int ROLE_USER = 0;
    public static final int ROLE_ADMIN = 1;

    /** role 为 null（老 token / 数据异常）也判成非管理员 —— 安全降级 */
    public boolean isAdmin() {
        return role != null && role == ROLE_ADMIN;
    }
}
