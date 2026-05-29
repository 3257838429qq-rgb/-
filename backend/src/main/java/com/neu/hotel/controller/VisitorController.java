package com.neu.hotel.controller;

import com.neu.hotel.common.annotation.Log;
import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.Visitor;
import com.neu.hotel.service.VisitorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 访客管理控制器
 *
 * 【功能说明】
 * - 管理访客预约登记
 * - 处理访客审核流程
 * - 记录访客来访信息
 *
 * 【API前缀】/visitor
 *
 * 【主要接口】
 * 1. 分页查询访客 - GET /visitor/page
 * 2. 待审核访客 - GET /visitor/pending
 * 3. 已审核访客 - GET /visitor/approved
 * 4. 获取访客详情 - GET /visitor/{id}
 * 5. 新增访客 - POST /visitor
 * 6. 修改访客 - PUT /visitor
 * 7. 审核访客 - PUT /visitor/review/{id}
 * 8. 删除访客 - DELETE /visitor/{id}
 *
 * 【关联关系】
 * - 依赖 VisitorService 处理访客业务逻辑
 * - 关联 Dept 实体（通过 hostDeptId）
 *
 * 【对应前端】
 * - 路由：/admin/visitor/list
 * - 路由：/portal/visit
 * - API：@/api/visitor/index.js
 * - Vue组件：@/views/visitor/list/index.vue, @/views/portal/Visit.vue
 */
@RestController
@RequestMapping("/visitor")
public class VisitorController {

    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    /**
     * 分页查询访客列表
     * GET /visitor/page?current=1&size=10&name=xxx&phone=xxx&status=1
     */
    @GetMapping("/page")
    public Result getPage(@RequestParam(defaultValue = "1") Long current,
                          @RequestParam(defaultValue = "10") Long size,
                          @RequestParam(required = false) String name,
                          @RequestParam(required = false) String phone,
                          @RequestParam(required = false) Integer status) {
        return Result.success(visitorService.selectAllVisitors(current, size, name, phone, status));
    }

    /**
     * 查询待审核的访客列表
     * GET /visitor/pending?current=1&size=10
     */
    @GetMapping("/pending")
    public Result getPending(@RequestParam(defaultValue = "1") Long current,
                             @RequestParam(defaultValue = "10") Long size) {
        return Result.success(visitorService.selectPendingVisitors(current, size));
    }

    /**
     * 查询已审核通过的访客列表
     * GET /visitor/approved?current=1&size=10
     */
    @GetMapping("/approved")
    public Result getApproved(@RequestParam(defaultValue = "1") Long current,
                              @RequestParam(defaultValue = "10") Long size) {
        return Result.success(visitorService.selectApprovedVisitors(current, size));
    }

    /**
     * 根据ID获取访客详情
     * GET /visitor/{id}
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(visitorService.getById(id));
    }

    /**
     * 新增访客记录
     * POST /visitor
     */
    @PostMapping
    @Log(title = "访客管理", businessType = "INSERT", operType = "新增访客")
    public Result add(@RequestBody Visitor visitor, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        visitor.setCreateUserId(userId);
        return Result.success(visitorService.save(visitor));
    }

    /**
     * 修改访客信息
     * PUT /visitor
     */
    @PutMapping
    @Log(title = "访客管理", businessType = "UPDATE", operType = "修改访客")
    public Result update(@RequestBody Visitor visitor) {
        return Result.success(visitorService.updateById(visitor));
    }

    /**
     * 审核访客申请
     * PUT /visitor/review/{id}?status=2&remark=xxx
     * status: 1=待审核, 2=已通过, 3=已拒绝
     */
    @PutMapping("/review/{id}")
    @Log(title = "访客管理", businessType = "UPDATE", operType = "审核访客预约")
    public Result review(@PathVariable Long id,
                         @RequestParam Integer status,
                         @RequestParam(required = false) String remark,
                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(visitorService.review(id, status, userId, remark));
    }

    /**
     * 删除访客记录
     * DELETE /visitor/{id}
     */
    @DeleteMapping("/{id}")
    @Log(title = "访客管理", businessType = "DELETE", operType = "删除访客")
    public Result delete(@PathVariable Long id) {
        return Result.success(visitorService.removeById(id));
    }
}
