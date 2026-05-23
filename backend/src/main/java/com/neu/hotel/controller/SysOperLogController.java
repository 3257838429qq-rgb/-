package com.neu.hotel.controller;

import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.SysOperLog;
import com.neu.hotel.service.SysOperLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/oper-log")
public class SysOperLogController {

    private final SysOperLogService sysOperLogService;

    public SysOperLogController(SysOperLogService sysOperLogService) {
        this.sysOperLogService = sysOperLogService;
    }

    @GetMapping("/page")
    public Result getPage(@RequestParam(defaultValue = "1") Long current,
                          @RequestParam(defaultValue = "10") Long size,
                          SysOperLog log) {
        return Result.success(sysOperLogService.selectPage(current, size, log));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.success(sysOperLogService.removeById(id));
    }

    @DeleteMapping("/batch")
    public Result batchDelete(@RequestParam List<Long> ids) {
        return Result.success(sysOperLogService.removeByIds(ids));
    }
}
