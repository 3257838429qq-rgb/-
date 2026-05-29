package com.neu.hotel.controller;

import com.neu.hotel.common.annotation.Log;
import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.Role;
import com.neu.hotel.service.RoleService;
import org.springframework.web.bind.annotation.*;

/**
 * 角色管理控制器
 *
 * 【功能说明】
 * - 管理系统角色
 * - 定义角色权限
 *
 * 【API前缀】/system/role
 *
 * 【主要接口】
 * 1. 获取角色列表 - GET /system/role/list
 * 2. 获取角色详情 - GET /system/role/{id}
 * 3. 新增角色 - POST /system/role
 * 4. 修改角色 - PUT /system/role
 * 5. 删除角色 - DELETE /system/role/{id}
 *
 * 【关联关系】
 * - 依赖 RoleService 处理角色业务
 * - 被 User 实体引用（通过 roleId）
 *
 * 【对应前端】
 * - 路由：/admin/system/role
 * - API：@/api/system/role.js
 * - Vue组件：@/views/system/role/index.vue
 */
@RestController
@RequestMapping("/system/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 获取所有角色列表
     * GET /system/role/list
     */
    @GetMapping("/list")
    public Result list() {
        return Result.success(roleService.selectAll());
    }

    /**
     * 根据ID获取角色详情
     * GET /system/role/{id}
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    /**
     * 新增角色
     * POST /system/role
     */
    @PostMapping
    @Log(title = "角色管理", businessType = "INSERT", operType = "新增")
    public Result add(@RequestBody Role role) {
        return Result.success(roleService.save(role));
    }

    /**
     * 修改角色
     * PUT /system/role
     */
    @PutMapping
    @Log(title = "角色管理", businessType = "UPDATE", operType = "修改")
    public Result update(@RequestBody Role role) {
        return Result.success(roleService.updateById(role));
    }

    /**
     * 删除角色
     * DELETE /system/role/{id}
     */
    @DeleteMapping("/{id}")
    @Log(title = "角色管理", businessType = "DELETE", operType = "删除")
    public Result delete(@PathVariable Long id) {
        return Result.success(roleService.removeById(id));
    }
}
