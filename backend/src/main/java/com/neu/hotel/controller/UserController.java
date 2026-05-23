package com.neu.hotel.controller;

import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.User;
import com.neu.hotel.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/system/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/page")
    public Result getPage(@RequestParam(defaultValue = "1") Long current,
                          @RequestParam(defaultValue = "10") Long size,
                          User user) {
        return Result.success(userService.selectPage(current, size, user));
    }

    @GetMapping("/list")
    public Result list() {
        return Result.success(userService.selectAll());
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @PostMapping
    public Result add(@RequestBody User user) {
        userService.register(user);
        return Result.success("新增成功");
    }

    @PutMapping
    public Result update(@RequestBody User user) {
        return Result.success(userService.updateById(user));
    }

    @PutMapping("/reset-password/{id}")
    public Result resetPassword(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(((com.neu.hotel.service.impl.UserServiceImpl) userService).encryptPassword("123456"));
        return Result.success(userService.updateById(user));
    }

    @PutMapping("/status/{id}")
    public Result updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        return Result.success(userService.updateById(user));
    }

    @PutMapping("/password")
    public Result updatePassword(@RequestParam Long userId,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword) {
        return Result.success(userService.updatePassword(userId, oldPassword, newPassword));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }
}
