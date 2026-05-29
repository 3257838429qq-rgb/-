package com.neu.hotel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 *
 * 【功能说明】
 * - 配置Spring MVC的相关组件
 * - 注册认证拦截器
 *
 * 【主要配置】
 * 1. 启用AOP自动代理（用于@Log注解日志切面）
 * 2. 注册AuthInterceptor认证拦截器
 *
 * 【拦截器配置】
 * - 拦截所有请求（/**）
 * - 排除无需登录的路径（如登录、注册、公共房间查询等）
 *
 * 【排除的路径】
 * - /auth/login, /auth/register: 登录注册
 * - /dorm/room/available, /dorm/room/page, /dorm/room-type/list: 公共房间信息
 * - /dorm/checkin/statistics, /dorm/checkin/active: 统计和入住数据
 * - /doc.html, /swagger-**: Swagger文档
 * - /error: 错误页面
 */
@Configuration
@EnableAspectJAutoProxy
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    /**
     * 注册认证拦截器
     * 定义哪些路径需要登录，哪些路径可以匿名访问
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(
                        // 认证相关（无需登录）
                        "/auth/login",
                        "/auth/register",
                        // 房间信息（公开访问）
                        "/dorm/room/available",
                        "/dorm/room/page",
                        "/dorm/room-type/list",
                        // 统计信息（公开访问）
                        "/dorm/checkin/statistics",
                        "/dorm/checkin/active",
                        // Swagger文档
                        "/doc.html",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        // 其他
                        "/favicon.ico",
                        "/error"
                );
    }
}
