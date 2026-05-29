package com.neu.hotel.controller;

import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.SysOperLog;
import com.neu.hotel.service.SysOperLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统操作日志控制器
 *
 * 【功能说明】
 * - 记录用户在系统中的操作行为
 * - 支持日志查询和清理
 * - 便于问题追溯和安全审计
 *
 * 【API前缀】/system/oper-log
 *
 * 【主要接口】
 * 1. 分页查询操作日志 - GET /system/oper-log/page
 * 2. 删除单条日志 - DELETE /system/oper-log/{id}
 * 3. 批量删除日志 - DELETE /system/oper-log/batch
 *
 * 【日志记录机制】
 * - 通过 @Log 注解自动记录操作
 * - 记录内容：操作人、操作时间、操作类型、操作内容、IP地址等
 *
 * 【关联关系】
 * - 依赖 SysOperLogService 处理日志业务
 * - 关联 SysOperLog 实体
 *
 * 【对应前端】
 * - 路由：/admin/system/log
 * - API：@/api/system/log.js
 * - Vue组件：@/views/system/log/index.vue
 */
@RestController
@RequestMapping("/system/oper-log")
public class SysOperLogController {

    private final SysOperLogService sysOperLogService;

    public SysOperLogController(SysOperLogService sysOperLogService) {
        this.sysOperLogService = sysOperLogService;
    }

    /**
     * 分页查询系统操作日志
     * GET /system/oper-log/page?current=1&size=10&operName=xxx
     * 支持按操作人、操作类型等条件筛选
     */
    @GetMapping("/page")
    public Result getPage(@RequestParam(defaultValue = "1") Long current,
                          @RequestParam(defaultValue = "10") Long size,
                          SysOperLog log) {
        return Result.success(sysOperLogService.selectPage(current, size, log));
    }

    /**
     * 删除单条操作日志
     * DELETE /system/oper-log/{id}
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.success(sysOperLogService.removeById(id));
    }

    /**
     * 批量删除操作日志
     * DELETE /system/oper-log/batch?ids=1,2,3
     */
    @DeleteMapping("/batch")
    public Result batchDelete(@RequestParam List<Long> ids) {
        return Result.success(sysOperLogService.removeByIds(ids));
    }
}
