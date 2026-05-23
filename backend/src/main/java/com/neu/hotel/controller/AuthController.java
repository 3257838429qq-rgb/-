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

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        User dbUser = userService.login(user.getUsername(), user.getPassword());
        if (dbUser != null) {
            Integer userType = dbUser.getUserType() != null ? dbUser.getUserType() : 0;
            String token = jwtUtil.generateToken(dbUser.getUsername(), dbUser.getId(), dbUser.getRoleId(), userType);
            List<Menu> menus = menuService.selectMenuTree(dbUser.getRoleId());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", dbUser);
            data.put("menus", menus);
            data.put("userType", userType);
            return Result.success(data);
        }
        return Result.error("用户名或密码错误");
    }

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        try {
            userService.register(user);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

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

    @PostMapping("/logout")
    public Result logout() {
        return Result.success("退出成功");
    }
}
