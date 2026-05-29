package com.neu.hotel.controller;

import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.Menu;
import com.neu.hotel.entity.User;
import com.neu.hotel.service.MenuService;
import com.neu.hotel.service.UserService;
import com.neu.hotel.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 *
 * 【功能说明】
 * - 处理用户登录、注册、退出等认证流程
 * - 生成和验证JWT Token
 * - 提供用户信息查询接口
 *
 * 【API前缀】/auth
 *
 * 【主要接口】
 * 1. 用户登录 - POST /auth/login
 * 2. 用户注册 - POST /auth/register
 * 3. 获取用户信息 - GET /auth/info
 * 4. 退出登录 - POST /auth/logout
 *
 * 【工作流程】
 * 1. 登录：验证用户名密码 -> 生成Token -> 返回用户信息和菜单
 * 2. 注册：验证数据 -> 创建用户 -> 返回成功
 * 3. 获取信息：解析Token -> 查询用户 -> 返回用户和菜单
 *
 * 【关联关系】
 * - 依赖 UserService 处理用户业务
 * - 依赖 MenuService 获取用户菜单权限
 * - 依赖 JwtUtil 生成和验证Token
 *
 * 【对应前端】
 * - API：@/api/auth.js
 * - Vue组件：@/views/login/index.vue
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     * POST /auth/login
     *
     * 请求体：{ "username": "xxx", "password": "xxx" }
     * 返回：{ token, user, menus, userType }
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        // 调用服务层验证登录
        User dbUser = userService.login(user.getUsername(), user.getPassword());
        if (dbUser != null) {
            // 生成JWT Token，包含用户ID、角色ID等信息
            Integer userType = dbUser.getUserType() != null ? dbUser.getUserType() : 0;
            String token = jwtUtil.generateToken(dbUser.getUsername(), dbUser.getId(), dbUser.getRoleId(), userType);
            // 获取该角色的菜单权限（树形结构）
            List<Menu> menus = menuService.selectMenuTree(dbUser.getRoleId());
            // 组装返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", dbUser);
            data.put("menus", menus);
            data.put("userType", userType);
            return Result.success(data);
        }
        return Result.error("用户名或密码错误");
    }

    /**
     * 用户注册
     * POST /auth/register
     *
     * 请求体：User对象
     * 返回：注册成功/失败信息
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        try {
            userService.register(user);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前登录用户信息
     * GET /auth/info
     *
     * 通过拦截器从Token中解析出userId，自动注入到request
     * 返回：{ user, menus, userType }
     */
    @GetMapping("/info")
    public Result getUserInfo(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        Long userId = (Long) request.getAttribute("userId");
        if (username != null) {
            User user = userService.getUserById(userId);
            if (user != null) {
                List<Menu> menus = menuService.selectMenuTree(user.getRoleId());
                Map<String, Object> data = new HashMap<>();
                data.put("user", user);
                data.put("menus", menus);
                data.put("userType", user.getUserType() != null ? user.getUserType() : 0);
                return Result.success(data);
            }
        }
        return Result.error("获取用户信息失败");
    }

    /**
     * 退出登录
     * POST /auth/logout
     *
     * 前端清除Token即可，后端可扩展黑名单机制
     */
    @PostMapping("/logout")
    public Result logout() {
        return Result.success("退出成功");
    }
}
