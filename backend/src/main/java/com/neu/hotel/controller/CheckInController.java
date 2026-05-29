package com.neu.hotel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neu.hotel.common.annotation.Log;
import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.CheckIn;
import com.neu.hotel.entity.Visitor;
import com.neu.hotel.mapper.VisitorMapper;
import com.neu.hotel.service.CheckInService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 入住管理控制器
 *
 * 【功能说明】
 * - 管理酒店的入住、退房、预订等业务流程
 * - 支持前台管理和用户自助预订
 * - 处理费用计算和支付记录
 *
 * 【API前缀】/dorm/checkin
 *
 * 【主要接口】
 * 1. 分页查询入住记录 - GET /page
 * 2. 查询当前入住列表 - GET /active
 * 3. 获取入住统计 - GET /statistics
 * 4. 新增入住 - POST /
 * 5. 退房操作 - PUT /checkout/{id}
 * 6. 记录支付 - PUT /pay/{id}
 * 7. 删除入住记录 - DELETE /{id}
 *
 * 【用户端接口】
 * 1. 提交预订 - POST /booking
 * 2. 提交退房申请 - POST /checkout-request/{id}
 * 3. 用户支付 - POST /user-pay/{id}
 * 4. 我的预订 - GET /my-bookings
 *
 * 【管理端审批接口】
 * 1. 待审批列表 - GET /pending
 * 2. 审批通过 - PUT /approve/{id}
 * 3. 审批拒绝 - PUT /reject/{id}
 * 4. 退房审批通过 - PUT /checkout-approve/{id}
 * 5. 退房审批拒绝 - PUT /checkout-reject/{id}
 *
 * 【关联关系】
 * - 依赖 CheckInService 处理入住业务逻辑
 * - 依赖 VisitorMapper 查询/创建访客信息
 * - 关联 DormRoom 实体（通过 roomId）
 * - 关联 Visitor 实体（通过 visitorId）
 *
 * 【对应前端】
 * - API：@/api/dorm/checkin.js
 * - Vue组件：@/views/dorm/checkin/index.vue
 * - 仪表盘统计：@/views/dashboard/index.vue
 */
@RestController
@RequestMapping("/dorm/checkin")
public class CheckInController {

    private final CheckInService checkInService;
    private final VisitorMapper visitorMapper;

    public CheckInController(CheckInService checkInService, VisitorMapper visitorMapper) {
        this.checkInService = checkInService;
        this.visitorMapper = visitorMapper;
    }

    // ==================== 基础查询接口 ====================

    /**
     * 分页查询所有入住记录
     * GET /dorm/checkin/page?current=1&size=10
     */
    @GetMapping("/page")
    public Result getPage(@RequestParam(defaultValue = "1") Long current,
                          @RequestParam(defaultValue = "10") Long size) {
        return Result.success(checkInService.selectPage(current, size, null));
    }

    /**
     * 查询当前正在入住的记录（状态为已入住）
     * GET /dorm/checkin/active?current=1&size=10
     * 用于仪表盘展示当前入住列表
     */
    @GetMapping("/active")
    public Result getActive(@RequestParam(defaultValue = "1") Long current,
                            @RequestParam(defaultValue = "10") Long size) {
        return Result.success(checkInService.selectActiveCheckIns(current, size));
    }

    /**
     * 获取入住统计数据
     * GET /dorm/checkin/statistics
     * 返回：当前入住数、空闲房间数、总房间数、入住率等
     */
    @GetMapping("/statistics")
    public Result getStatistics() {
        return Result.success(checkInService.getStatistics());
    }

    /**
     * 根据ID获取入住详情
     * GET /dorm/checkin/{id}
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(checkInService.getById(id));
    }

    // ==================== 入住操作接口 ====================

    /**
     * 新增入住记录（前台操作员使用）
     * POST /dorm/checkin
     * 逻辑：根据手机号查找或创建访客，创建入住记录
     */
    @PostMapping
    @Log(title = "入住管理", businessType = "INSERT", operType = "新增入住")
    public Result add(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 从请求中获取当前登录用户ID
        Long userId = (Long) request.getAttribute("userId");
        String visitorName = (String) params.get("visitorName");
        String phone = (String) params.get("phone");
        String idCard = (String) params.get("idCard");

        // 根据手机号查找访客，如不存在则创建新访客
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Visitor::getPhone, phone);
        Visitor visitor = visitorMapper.selectOne(wrapper);
        if (visitor == null) {
            visitor = new Visitor();
            visitor.setName(visitorName);
            visitor.setPhone(phone);
            visitor.setIdCard(idCard);
            visitor.setStatus(1); // 状态设为"已通过"
            visitorMapper.insert(visitor);
        } else if (idCard != null && !idCard.isEmpty()) {
            // 更新已有访客的身份证信息
            visitor.setIdCard(idCard);
            visitorMapper.updateById(visitor);
        }

        // 构建入住记录
        CheckIn checkIn = new CheckIn();
        checkIn.setVisitorId(visitor.getId());
        checkIn.setIdCard(idCard);
        checkIn.setRoomId(toLong(params.get("roomId")));
        checkIn.setRemark((String) params.get("remark"));

        // 解析入住和退房日期
        Object checkInDate = params.get("checkInDate");
        if (checkInDate != null) checkIn.setCheckInDate(LocalDate.parse(checkInDate.toString()));

        Object checkOutDate = params.get("checkOutDate");
        if (checkOutDate != null) checkIn.setCheckOutDate(LocalDate.parse(checkOutDate.toString()));

        // 调用服务层完成入住
        return Result.success(checkInService.checkIn(checkIn, userId));
    }

    /**
     * 工具方法：将Object转换为Long类型
     */
    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.valueOf(obj.toString());
    }

    /**
     * 退房操作（前台操作员使用）
     * PUT /dorm/checkin/checkout/{id}
     * 设置额外费用，完成退房
     */
    @PutMapping("/checkout/{id}")
    @Log(title = "入住管理", businessType = "UPDATE", operType = "退房")
    public Result checkOut(@PathVariable Long id,
                           @RequestBody Map<String, Object> body,
                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BigDecimal otherFee = body.get("otherFee") != null ? new BigDecimal(body.get("otherFee").toString()) : BigDecimal.ZERO;
        String paymentMethod = (String) body.get("paymentMethod");
        return Result.success(checkInService.checkOut(id, userId, otherFee, paymentMethod));
    }

    /**
     * 记录支付信息
     * PUT /dorm/checkin/pay/{id}
     */
    @PutMapping("/pay/{id}")
    @Log(title = "入住管理", businessType = "UPDATE", operType = "记录支付")
    public Result recordPayment(@PathVariable Long id,
                                @RequestBody Map<String, Object> body) {
        BigDecimal paidAmount = new BigDecimal(body.get("paidAmount").toString());
        String paymentMethod = (String) body.get("paymentMethod");
        return Result.success(checkInService.recordPayment(id, paidAmount, paymentMethod));
    }

    /**
     * 删除入住记录
     * DELETE /dorm/checkin/{id}
     */
    @DeleteMapping("/{id}")
    @Log(title = "入住管理", businessType = "DELETE", operType = "删除记录")
    public Result delete(@PathVariable Long id) {
        return Result.success(checkInService.removeById(id));
    }

    // ==================== 用户端预订接口 ====================

    /**
     * 用户提交预订申请（创建待审核的入住记录）
     * POST /dorm/checkin/booking
     */
    @PostMapping("/booking")
    @Log(title = "预订管理", businessType = "INSERT", operType = "提交预订")
    public Result submitBooking(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String visitorName = (String) params.get("visitorName");
        String phone = (String) params.get("phone");
        String idCard = (String) params.get("idCard");

        // 同上的访客查找/创建逻辑
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Visitor::getPhone, phone);
        Visitor visitor = visitorMapper.selectOne(wrapper);
        if (visitor == null) {
            visitor = new Visitor();
            visitor.setName(visitorName);
            visitor.setPhone(phone);
            visitor.setIdCard(idCard);
            visitor.setStatus(1);
            visitorMapper.insert(visitor);
        } else if (idCard != null && !idCard.isEmpty()) {
            visitor.setIdCard(idCard);
            visitorMapper.updateById(visitor);
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setVisitorId(visitor.getId());
        checkIn.setIdCard(idCard);
        checkIn.setRoomId(toLong(params.get("roomId")));
        checkIn.setRemark((String) params.get("remark"));

        Object checkInDate = params.get("checkInDate");
        if (checkInDate != null) checkIn.setCheckInDate(LocalDate.parse(checkInDate.toString()));

        Object checkOutDate = params.get("checkOutDate");
        if (checkOutDate != null) checkIn.setCheckOutDate(LocalDate.parse(checkOutDate.toString()));

        return Result.success(checkInService.submitBooking(checkIn, userId));
    }

    /**
     * 用户提交退房申请（不自己设定费用，等待管理员审核）
     * POST /dorm/checkin/checkout-request/{id}
     */
    @PostMapping("/checkout-request/{id}")
    @Log(title = "预订管理", businessType = "UPDATE", operType = "申请退房")
    public Result submitCheckoutRequest(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.submitCheckoutRequest(id, userId));
    }

    /**
     * 用户付款
     * POST /dorm/checkin/user-pay/{id}
     */
    @PostMapping("/user-pay/{id}")
    @Log(title = "预订管理", businessType = "UPDATE", operType = "用户支付")
    public Result userPay(@PathVariable Long id,
                          @RequestBody Map<String, Object> body,
                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        CheckIn record = checkInService.getById(id);
        // 验证权限：只有创建该订单的用户可以付款
        if (record == null || !userId.equals(record.getCheckInUserId())) {
            return Result.error(403, "无权操作");
        }
        BigDecimal paidAmount = body.get("paidAmount") != null
                ? new BigDecimal(body.get("paidAmount").toString())
                : record.getRoomFee();
        String paymentMethod = (String) body.get("paymentMethod");
        return Result.success(checkInService.recordPayment(id, paidAmount, paymentMethod));
    }

    /**
     * 获取当前用户的预订记录
     * GET /dorm/checkin/my-bookings?current=1&size=10
     */
    @GetMapping("/my-bookings")
    public Result getMyBookings(@RequestParam(defaultValue = "1") Long current,
                                @RequestParam(defaultValue = "10") Long size,
                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.selectMyBookings(current, size, userId));
    }

    // ==================== 管理端审批接口 ====================

    /**
     * 获取待审批的预订列表
     * GET /dorm/checkin/pending?current=1&size=10&status=1
     */
    @GetMapping("/pending")
    public Result getPending(@RequestParam(defaultValue = "1") Long current,
                              @RequestParam(defaultValue = "10") Long size,
                              @RequestParam(required = false) Integer status) {
        return Result.success(checkInService.selectPendingCheckIns(current, size, status));
    }

    /**
     * 审批通过预订申请
     * PUT /dorm/checkin/approve/{id}
     */
    @PutMapping("/approve/{id}")
    @Log(title = "审批管理", businessType = "UPDATE", operType = "审批通过")
    public Result approve(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.approveBooking(id, userId));
    }

    /**
     * 审批拒绝预订申请
     * PUT /dorm/checkin/reject/{id}?reason=xxx
     */
    @PutMapping("/reject/{id}")
    @Log(title = "审批管理", businessType = "UPDATE", operType = "审批拒绝")
    public Result reject(@PathVariable Long id,
                         @RequestParam(required = false) String reason,
                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.rejectBooking(id, userId, reason));
    }

    /**
     * 审核通过退房申请，设置额外费用，完成退房
     * PUT /dorm/checkin/checkout-approve/{id}
     */
    @PutMapping("/checkout-approve/{id}")
    @Log(title = "审批管理", businessType = "UPDATE", operType = "退房审批通过")
    public Result approveCheckout(@PathVariable Long id,
                                  @RequestBody Map<String, Object> body,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BigDecimal otherFee = body.get("otherFee") != null ? new BigDecimal(body.get("otherFee").toString()) : BigDecimal.ZERO;
        String paymentMethod = (String) body.get("paymentMethod");
        return Result.success(checkInService.approveCheckoutRequest(id, userId, otherFee, paymentMethod));
    }

    /**
     * 审核拒绝退房申请，退房申请被取消，入住状态恢复为已入住
     * PUT /dorm/checkin/checkout-reject/{id}
     */
    @PutMapping("/checkout-reject/{id}")
    @Log(title = "审批管理", businessType = "UPDATE", operType = "退房审批拒绝")
    public Result rejectCheckout(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.rejectCheckoutRequest(id, userId));
    }
}
