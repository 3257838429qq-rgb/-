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

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (StrUtil.isBlank(token)) {
            sendUnauthorizedResponse(response, "未登录，请先登录");
            return false;
        }

        if (!token.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "Token格式错误");
            return false;
        }

        token = token.substring(7);
        String username = jwtUtil.parseToken(token);
        if (username == null) {
            sendUnauthorizedResponse(response, "Token已过期，请重新登录");
            return false;
        }

        request.setAttribute("username", username);
        request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
        request.setAttribute("roleId", jwtUtil.getRoleIdFromToken(token));
        request.setAttribute("userType", jwtUtil.getUserTypeFromToken(token));
        return true;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(Result.unauthorized(message)));
    }
}
