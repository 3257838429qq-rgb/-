package com.neu.hotel.controller;

import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.Visitor;
import com.neu.hotel.service.VisitorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/visitor")
public class VisitorController {

    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @GetMapping("/page")
    public Result getPage(@RequestParam(defaultValue = "1") Long current,
                          @RequestParam(defaultValue = "10") Long size,
                          @RequestParam(required = false) String name,
                          @RequestParam(required = false) String phone,
                          @RequestParam(required = false) Integer status) {
        return Result.success(visitorService.selectAllVisitors(current, size, name, phone, status));
    }

    @GetMapping("/pending")
    public Result getPending(@RequestParam(defaultValue = "1") Long current,
                             @RequestParam(defaultValue = "10") Long size) {
        return Result.success(visitorService.selectPendingVisitors(current, size));
    }

    @GetMapping("/approved")
    public Result getApproved(@RequestParam(defaultValue = "1") Long current,
                              @RequestParam(defaultValue = "10") Long size) {
        return Result.success(visitorService.selectApprovedVisitors(current, size));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(visitorService.getById(id));
    }

    @PostMapping
    public Result add(@RequestBody Visitor visitor, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        visitor.setCreateUserId(userId);
        return Result.success(visitorService.save(visitor));
    }

    @PutMapping
    public Result update(@RequestBody Visitor visitor) {
        return Result.success(visitorService.updateById(visitor));
    }

    @PutMapping("/review/{id}")
    public Result review(@PathVariable Long id,
                         @RequestParam Integer status,
                         @RequestParam(required = false) String remark,
                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(visitorService.review(id, status, userId, remark));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.success(visitorService.removeById(id));
    }
}
