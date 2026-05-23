package com.neu.hotel.controller;

import com.neu.hotel.common.annotation.Log;
import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.Dept;
import com.neu.hotel.service.DeptService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dept")
public class DeptController {

    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    @GetMapping("/list")
    public Result<List<Dept>> list() {
        return Result.success(deptService.selectTree());
    }

    @GetMapping("/{id}")
    public Result<Dept> getById(@PathVariable Long id) {
        return Result.success(deptService.getById(id));
    }

    @PostMapping
    @Log(title = "部门管理", businessType = "INSERT", operType = "新增")
    public Result<Boolean> add(@RequestBody Dept dept) {
        dept.setStatus(dept.getStatus() == null ? 1 : dept.getStatus());
        return Result.success(deptService.save(dept));
    }

    @PutMapping
    @Log(title = "部门管理", businessType = "UPDATE", operType = "修改")
    public Result<Boolean> update(@RequestBody Dept dept) {
        return Result.success(deptService.updateById(dept));
    }

    @DeleteMapping("/{id}")
    @Log(title = "部门管理", businessType = "DELETE", operType = "删除")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(deptService.removeById(id));
    }
}
