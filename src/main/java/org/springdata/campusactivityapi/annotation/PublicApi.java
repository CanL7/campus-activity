package org.springdata.campusactivityapi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标在 Controller 方法上，表示"这个接口匿名就能访问，不需要登录"。
 *
 * 为什么需要它 —— 拦截器默认策略的问题：
 *   JwtInterceptor 注册在 /** 上，默认所有接口都要求"带 token"。
 *   但像"浏览活动列表""查看活动详情"这种，游客也应该能看。
 *
 *   两种实现思路：
 *     A. 白名单写在 WebMvcConfig 的 excludePathPatterns 里
 *        → 按 URL 匹配，接口一多就要维护一长串路径，改 URL 还容易忘
 *     B. 注解贴在方法上（就是本注解）
 *        → 权限声明和接口代码在同一个地方，加接口时顺手就标了，不会漏
 *
 * 我们用 B。注解只有一个方法级作用，判断逻辑在 JwtInterceptor。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PublicApi {
}
