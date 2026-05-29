package com.neu.hotel.config;

import cn.hutool.core.util.StrUtil;
import com.neu.hotel.common.result.Result;
import com.neu.hotel.common.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 认证拦截器
 *
 * 【功能说明】
 * - 验证请求中的JWT Token
 * - 解析Token并注入用户信息到request
 * - 拦截未登录或Token过期的请求
 *
 * 【工作流程】
 * 1. 从请求头获取Authorization字段
 * 2. 验证Token格式（Bearer xxx）
 * 3. 调用JwtUtil解析Token
 * 4. 解析成功则注入userId、username、roleId、userType到request
 * 5. 失败则返回401未授权错误
 *
 * 【Token信息】
 * - 解析后注入request的属性：
 *   - username: 用户名
 *   - userId: 用户ID
 *   - roleId: 角色ID
 *   - userType: 用户类型（0=普通用户, 1=管理员）
 *
 * 【错误处理】
 * - Token为空: "未登录，请先登录"
 * - Token格式错误: "Token格式错误"
 * - Token过期: "Token已过期，请重新登录"
 *
 * 【配合使用】
 * - JwtUtil: Token生成和解析工具
 * - WebConfig: 拦截器注册配置
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 前置处理：验证Token
     * @return true=放行，false=拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        // 检查Token是否为空
        if (StrUtil.isBlank(token)) {
            sendUnauthorizedResponse(response, "未登录，请先登录");
            return false;
        }

        // 检查Token格式（Bearer xxx）
        if (!token.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "Token格式错误");
            return false;
        }

        // 去掉"Bearer "前缀
        token = token.substring(7);
        String username = jwtUtil.parseToken(token);
        if (username == null) {
            sendUnauthorizedResponse(response, "Token已过期，请重新登录");
            return false;
        }

        // 解析成功，注入用户信息到request
        request.setAttribute("username", username);
        request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
        request.setAttribute("roleId", jwtUtil.getRoleIdFromToken(token));
        request.setAttribute("userType", jwtUtil.getUserTypeFromToken(token));
        return true;
    }

    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);  // 返回200，错误信息在body中
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(Result.unauthorized(message)));
    }
}
