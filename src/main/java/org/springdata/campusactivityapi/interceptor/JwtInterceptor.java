package org.springdata.campusactivityapi.interceptor;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springdata.campusactivityapi.annotation.PublicApi;
import org.springdata.campusactivityapi.annotation.RequireAdmin;
import org.springdata.campusactivityapi.common.CurrentUser;
import org.springdata.campusactivityapi.common.LoginUser;
import org.springdata.campusactivityapi.exception.ForbiddenException;
import org.springdata.campusactivityapi.exception.UnauthorizedException;
import org.springdata.campusactivityapi.util.JwtUtil;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.annotation.Annotation;


/**
 * 两道关卡，都在 Controller 之前：
 *   1. 认证（Authentication）—— 你带了有效 token 吗？没有 → 401
 *   2. 授权（Authorization） —— 这方法要管理员，你是吗？不是 → 403
 *
 * 这里抛出的异常同样会被 GlobalExceptionHandler 接住
 * （Spring MVC 的异常解析链覆盖拦截器阶段），所以不用自己往 response 里写东西。
 *
 * 为什么用 Interceptor 而不是 Filter —— 现在能兑现了：
 *   Interceptor 的 preHandle 第三个参数是 HandlerMethod，能拿到"马上要执行哪个方法"，
 *   于是可以读方法上的 @RequireAdmin 注解。Filter 只拿到 URL，读不到注解，
 *   想做同样的判断就得在 Filter 里自己维护一张"哪些 URL 要管理员"的表 —— 加接口就要改表，必漏。
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Controller 之前执行。
     * 返回 true = 放行；返回 false = 中断（Controller 不执行）。
     * 我们不用 false，而是直接抛异常 —— 异常会被全局处理器转成统一格式的 401/403。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 浏览器的跨域预检请求（OPTIONS）不带 Authorization，必须放行，否则所有跨域调用都会挂
        if(HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        // 先看这个方法贴没贴 @PublicApi —— 贴了就直接放行，连 token 都不用带。
        // 必须在"要 token"的判断之前，否则匿名请求会先被 401 拦掉
        if (hasAnnotation(handler, PublicApi.class)) {
            return true;
        }
        //这里搞token 从header里拿 -> request拿
        String header = request.getHeader("Authorization");
        //判断一下 如果为空 或者不是以Bearer 开头的 就不是正常token
        if (header == null || !header.startsWith(PREFIX)) {
            throw new UnauthorizedException("未登录");
        }
        //正常的 截取前面Prefix的长度
        String token = header.substring(PREFIX.length());

        LoginUser loginUser;
        try {
            // 验签 + 解出 {userId, role}，一步到位，不用查库
            loginUser = jwtUtil.parse(token);

        } catch (JwtException | IllegalArgumentException e) {
            // 过期 / 签名不对 / 格式错：一律当成"没登录"。
            // 关键：把底层异常吃掉，换成自己的业务异常 —— 否则会被兜底 handler 变成 500
            throw new UnauthorizedException("登录已失效，请重新登录");
        }

        // ★ 认证通过，先把身份放进 ThreadLocal（后面无论走哪条路径，afterCompletion 都会清理）
        CurrentUser.set(loginUser);

        // ★ 授权：看"即将执行的方法"上有没有 @RequireAdmin 还有用户是不是管理员
        if (hasAnnotation(handler, RequireAdmin.class) && !loginUser.isAdmin()) {
            throw new ForbiddenException("需要管理员权限");
        }
        return true;
    }

    /**
     * 读"即将执行的方法"上有没有指定注解。
     *
     * handler 不是永远都是 HandlerMethod：静态资源（css/js/图片）会被
     * ResourceHttpRequestHandler 处理，那种 handler 没有方法可读 —— 返回 false（视为无注解）。
     */
    private boolean hasAnnotation(Object handler, Class<? extends Annotation> annotationType) {
        //因为只有HandlerMethod能读取 所以要看看handler是不是
        if (!(handler instanceof HandlerMethod handlerMethod)){
            return false;//不是就false
        }

        // getMethodAnnotation 读的是"目标方法"上的注解。
        // 想支持贴在类上（整个 Controller 统一要求），换成 getBeanType().getAnnotation(...) 即可
        return handlerMethod.getMethodAnnotation(annotationType) != null;
    }

    /** 请求彻底结束后执行 —— 清理 ThreadLocal */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // ★ 清理线程
        CurrentUser.remove();

    }
}
