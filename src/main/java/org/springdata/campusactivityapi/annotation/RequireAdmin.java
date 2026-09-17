package org.springdata.campusactivityapi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标在 Controller 方法上，表示"这个方法只有管理员能调"。
 *
 * 三个元注解各自解决一个问题：
 *   @Target(METHOD)    —— 只能贴方法，贴到类/字段上编译器直接报错
 *   @Retention(RUNTIME)—— ★ 关键：注解默认在 .class 文件里不留（SOURCE/CLASS），
 *                          只有 RUNTIME 才能被反射读出来。
 *                          拦截器正是靠 getMethodAnnotation() 反射读它 —— 读不到就白搭。
 *   @Documented        —— 纯粹是生成 javadoc 时带上，可加可不加，这里省了
 *
 * 注解本身不含任何逻辑：它只是个"标记"。
 * 真正的判断逻辑在 JwtInterceptor 里 —— 注解负责"声明意图"，
 * 拦截器负责"执行规则"，两者分离，加权限只需在方法上贴一行。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdmin {
}
