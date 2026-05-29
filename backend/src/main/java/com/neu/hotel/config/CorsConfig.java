package com.neu.hotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 *
 * 【功能说明】
 * - 配置CORS（跨域资源共享）策略
 * - 允许前端应用跨域访问后端API
 *
 * 【配置说明】
 * - allowCredentials: 允许携带认证信息（如Cookie）
 * - allowedOriginPatterns: 允许的来源（*表示所有）
 * - allowedHeaders: 允许的请求头（*表示所有）
 * - allowedMethods: 允许的HTTP方法（*表示所有）
 * - maxAge: 预检请求缓存时间（1小时）
 *
 * 【工作原理】
 * - 为所有路径（/**）应用CORS配置
 * - 自动处理OPTIONS预检请求
 *
 * 【前端对接】
 * - Vue项目需要在axios配置baseURL和withCredentials
 * - 后端返回的Access-Control-Allow-Origin头使跨域请求可行
 */
@Configuration
public class CorsConfig {

    /**
     * 创建CORS过滤器
     * @return CorsFilter实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);  // 允许携带认证信息
        config.addAllowedOriginPattern("*");  // 允许所有来源
        config.addAllowedHeader("*");  // 允许所有请求头
        config.addAllowedMethod("*");  // 允许所有HTTP方法
        config.setMaxAge(3600L);  // 预检请求缓存1小时

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);  // 所有路径应用CORS配置
        return new CorsFilter(source);
    }
}
