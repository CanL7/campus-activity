package org.springdata.campusactivityapi.common;

/**
 * "当前请求是谁"的载体。
 *
 * 为什么用 ThreadLocal：
 *   一个 HTTP 请求自始至终跑在同一个线程上，所以在这个线程的任意位置
 *   （Controller、Service、Mapper）都能读到它 —— 不用一路当参数往下传。
 *
 * 生命周期由 JwtInterceptor 控制：
 *   preHandle          → set(loginUser)
 *   Controller/Service → get() / getUserId()
 *   afterCompletion    → remove()   ★ 必须！
 *
 * 不 remove 的后果：Tomcat 线程是复用的，下一个请求可能落到同一个线程上，
 * 于是新用户会读到上一个用户的身份 —— 极难复现的身份串号 bug。
 *
 * 存 LoginUser（含 role）而不是裸 Long userId：
 *   权限判断要用 role，从 token 解出来的东西直接放这儿，
 *   拦截器和业务代码都不用再查库。
 */
public final class CurrentUser {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private CurrentUser() {   // 工具类，不让 new
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    /** 完整信息（含角色）。没登录时返回 null；正常流程里拦截器已经挡在门外了 */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 只取 userId —— 业务代码用得最多的就是这个，单独开一个方法免得到处写 get().userId()。
     * 没登录时抛异常而不是返回 null：如果走到这儿还是 null，说明拦截器白名单配错了，
     * 属于编码错误，早炸早发现（返回 null 会变成后面某行莫名其妙的 NPE）。
     */
    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new IllegalStateException("当前请求没有登录信息，请检查拦截器白名单配置");
        }
        return user.userId();
    }

    public static void remove() {
        HOLDER.remove();
    }
}
