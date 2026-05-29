package com.neu.hotel.controller;

import com.neu.hotel.common.annotation.Log;
import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.Dept;
import com.neu.hotel.service.DeptService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 *
 * 【功能说明】
 * - 管理酒店内部组织架构
 * - 支持树形结构展示
 * - 记录部门负责人信息
 *
 * 【API前缀】/system/dept
 *
 * 【主要接口】
 * 1. 获取部门树形列表 - GET /system/dept/list
 * 2. 获取部门详情 - GET /system/dept/{id}
 * 3. 新增部门 - POST /system/dept
 * 4. 修改部门 - PUT /system/dept
 * 5. 删除部门 - DELETE /system/dept/{id}
 *
 * 【关联关系】
 * - 依赖 DeptService 处理部门业务
 * - 被 Visitor 实体引用（通过 hostDeptId）
 *
 * 【对应前端】
 * - 路由：/admin/system/dept
 * - API：@/api/system/dept.js
 * - Vue组件：@/views/system/dept/index.vue
 *
 * 【日志记录】
 * - 使用 @Log 注解记录增删改操作
 */
@RestController
@RequestMapping("/system/dept")
public class DeptController {

    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    /**
     * 获取部门树形列表
     * GET /system/dept/list
     * 返回树形结构，支持前端Tree组件渲染
     */
    @GetMapping("/list")
    public Result<List<Dept>> list() {
        return Result.success(deptService.selectTree());
    }

    /**
     * 根据ID获取部门详情
     * GET /system/dept/{id}
     */
    @GetMapping("/{id}")
    public Result<Dept> getById(@PathVariable Long id) {
        return Result.success(deptService.getById(id));
    }

    /**
     * 新增部门
     * POST /system/dept
     * 操作会被记录到系统日志
     */
    @PostMapping
    @Log(title = "部门管理", businessType = "INSERT", operType = "新增")
    public Result<Boolean> add(@RequestBody Dept dept) {
        dept.setStatus(dept.getStatus() == null ? 1 : dept.getStatus());
        return Result.success(deptService.save(dept));
    }

    /**
     * 修改部门
     * PUT /system/dept
     * 操作会被记录到系统日志
     */
    @PutMapping
    @Log(title = "部门管理", businessType = "UPDATE", operType = "修改")
    public Result<Boolean> update(@RequestBody Dept dept) {
        return Result.success(deptService.updateById(dept));
    }

    /**
     * 删除部门
     * DELETE /system/dept/{id}
     * 操作会被记录到系统日志
     */
    @DeleteMapping("/{id}")
    @Log(title = "部门管理", businessType = "DELETE", operType = "删除")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(deptService.removeById(id));
    }
}
