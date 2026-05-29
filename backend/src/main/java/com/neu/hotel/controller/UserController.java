package com.neu.hotel.controller;

import com.neu.hotel.common.annotation.Log;
import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.User;
import com.neu.hotel.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器
 *
 * 【功能说明】
 * - 管理系统用户（CRUD）
 * - 处理用户密码重置和修改
 * - 管理用户状态启用/禁用
 *
 * 【API前缀】/system/user
 *
 * 【主要接口】
 * 1. 分页查询用户 - GET /system/user/page
 * 2. 获取用户列表 - GET /system/user/list
 * 3. 获取用户详情 - GET /system/user/{id}
 * 4. 新增用户 - POST /system/user
 * 5. 修改用户 - PUT /system/user
 * 6. 重置密码 - PUT /system/user/reset-password/{id}
 * 7. 修改状态 - PUT /system/user/status/{id}
 * 8. 修改密码 - PUT /system/user/password
 * 9. 删除用户 - DELETE /system/user/{id}
 *
 * 【关联关系】
 * - 依赖 UserService 处理用户业务逻辑
 * - 关联 Role 实体（通过 roleId）
 *
 * 【对应前端】
 * - 路由：/admin/system/user
 * - API：@/api/system/user.js
 * - Vue组件：@/views/system/user/index.vue
 */
@RestController
@RequestMapping("/system/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 分页查询用户列表
     * GET /system/user/page?current=1&size=10&username=xxx&roleId=1
     */
    @GetMapping("/page")
    public Result getPage(@RequestParam(defaultValue = "1") Long current,
                          @RequestParam(defaultValue = "10") Long size,
                          User user) {
        return Result.success(userService.selectPage(current, size, user));
    }

    /**
     * 获取所有用户列表（下拉框等场景使用）
     * GET /system/user/list
     */
    @GetMapping("/list")
    public Result list() {
        return Result.success(userService.selectAll());
    }

    /**
     * 根据ID获取用户详情
     * GET /system/user/{id}
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    /**
     * 新增用户（注册）
     * POST /system/user
     * 会调用UserService.register进行密码加密等处理
     */
    @PostMapping
    @Log(title = "用户管理", businessType = "INSERT", operType = "新增")
    public Result add(@RequestBody User user) {
        userService.register(user);
        return Result.success("新增成功");
    }

    /**
     * 修改用户信息
     * PUT /system/user
     */
    @PutMapping
    @Log(title = "用户管理", businessType = "UPDATE", operType = "修改")
    public Result update(@RequestBody User user) {
        return Result.success(userService.updateById(user));
    }

    /**
     * 重置用户密码为默认密码123456
     * PUT /system/user/reset-password/{id}
     */
    @PutMapping("/reset-password/{id}")
    @Log(title = "用户管理", businessType = "UPDATE", operType = "重置密码")
    public Result resetPassword(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 加密默认密码123456
        user.setPassword(((com.neu.hotel.service.impl.UserServiceImpl) userService).encryptPassword("123456"));
        return Result.success(userService.updateById(user));
    }

    /**
     * 修改用户状态（启用/禁用）
     * PUT /system/user/status/{id}?status=1
     */
    @PutMapping("/status/{id}")
    @Log(title = "用户管理", businessType = "UPDATE", operType = "修改状态")
    public Result updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        return Result.success(userService.updateById(user));
    }

    /**
     * 用户修改自己的密码
     * PUT /system/user/password?userId=1&oldPassword=xxx&newPassword=xxx
     */
    @PutMapping("/password")
    @Log(title = "用户管理", businessType = "UPDATE", operType = "修改密码")
    public Result updatePassword(@RequestParam Long userId,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword) {
        return Result.success(userService.updatePassword(userId, oldPassword, newPassword));
    }

    /**
     * 删除用户
     * DELETE /system/user/{id}
     */
    @DeleteMapping("/{id}")
    @Log(title = "用户管理", businessType = "DELETE", operType = "删除")
    public Result delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }
}
