package com.neu.hotel.controller;

import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.Role;
import com.neu.hotel.service.RoleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/list")
    public Result list() {
        return Result.success(roleService.selectAll());
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @PostMapping
    public Result add(@RequestBody Role role) {
        return Result.success(roleService.save(role));
    }

    @PutMapping
    public Result update(@RequestBody Role role) {
        return Result.success(roleService.updateById(role));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.success(roleService.removeById(id));
    }
}
