package org.springdata.campusactivityapi.config;

import org.springdata.campusactivityapi.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册拦截器 + 白名单。
 *
 * 注意区分两种 @Configuration 的用途：
 *   SecurityConfig —— 用 @Bean 生产对象（BCryptPasswordEncoder）交给容器
 *   WebMvcConfig  —— 实现 WebMvcConfigurer 接口，往 Spring MVC 上"挂"东西（拦截器）
 * 两者本质都是"配置类"，但一个管 Bean，一个管框架装配。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    public WebMvcConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")                       // 默认全拦
                .excludePathPatterns("/auth/**", "/error");   // 白名单：注册、登录不拦
        // 白名单只写一行 /auth/** —— 这就是把注册登录单独拆成 AuthController 的回报
    }
}
