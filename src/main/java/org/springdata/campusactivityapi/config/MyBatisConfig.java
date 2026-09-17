package org.springdata.campusactivityapi.config;

import com.github.pagehelper.PageInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 注册 PageHelper 分页插件。
 *
 * 为什么是手动注册，而不是引入 pagehelper-spring-boot-starter：
 *   1. starter 的自动配置类是给 Spring Boot 2/3 写的，本项目是 Spring Boot 4，
 *      自动配置不保证生效（引入后可能启动就报错）。
 *   2. 而 PageHelper 本质是一个 MyBatis 插件（实现 org.apache.ibatis.plugin.Interceptor），
 *      跟 Spring Boot 版本无关 —— 只要 MyBatis 版本对得上就能用。
 *      （已确认：pagehelper 6.1.1 声明依赖 mybatis 3.5.19，与本项目一致）
 *
 * 为什么注册成 Bean 就够了：
 *   mybatis-spring-boot-starter 在构建 SqlSessionFactory 时，
 *   会自动把容器里所有 Interceptor 类型的 Bean 收集起来挂到插件链上，
 *   所以我们不需要自己 new SqlSessionFactory。
 */
@Configuration
public class MyBatisConfig {

    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor interceptor = new PageInterceptor();

        Properties props = new Properties();
        // 方言：不写也能自动探测，但显式指定更保险（项目就是 MySQL）
        props.setProperty("helperDialect", "mysql");
        // reasonable：页码 < 1 查第一页，页码 > 总页数查最后一页（而不是返回空列表）
        props.setProperty("reasonable", "true");
        // 允许把分页参数从方法参数里取（本项目不用，留默认即可）
        interceptor.setProperties(props);

        return interceptor;
    }
}
