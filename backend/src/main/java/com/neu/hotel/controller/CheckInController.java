package com.neu.hotel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

@RestController
@RequestMapping("/dorm/checkin")
public class CheckInController {

    private final CheckInService checkInService;
    private final VisitorMapper visitorMapper;

    public CheckInController(CheckInService checkInService, VisitorMapper visitorMapper) {
        this.checkInService = checkInService;
        this.visitorMapper = visitorMapper;
    }

    @GetMapping("/page")
    public Result getPage(@RequestParam(defaultValue = "1") Long current,
                          @RequestParam(defaultValue = "10") Long size) {
        return Result.success(checkInService.selectPage(current, size, null));
    }

    @GetMapping("/active")
    public Result getActive(@RequestParam(defaultValue = "1") Long current,
                            @RequestParam(defaultValue = "10") Long size) {
        return Result.success(checkInService.selectActiveCheckIns(current, size));
    }

    @GetMapping("/statistics")
    public Result getStatistics() {
        return Result.success(checkInService.getStatistics());
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(checkInService.getById(id));
    }

    @PostMapping
    public Result add(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String visitorName = (String) params.get("visitorName");
        String phone = (String) params.get("phone");
        String idCard = (String) params.get("idCard");

        // 查找或创建访客
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Visitor::getPhone, phone);
        Visitor visitor = visitorMapper.selectOne(wrapper);
        if (visitor == null) {
            visitor = new Visitor();
            visitor.setName(visitorName);
            visitor.setPhone(phone);
            visitor.setIdCard(idCard);
            visitor.setStatus(1); // 已通过
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

        return Result.success(checkInService.checkIn(checkIn, userId));
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.valueOf(obj.toString());
    }

    @PutMapping("/checkout/{id}")
    public Result checkOut(@PathVariable Long id,
                           @RequestBody Map<String, Object> body,
                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BigDecimal otherFee = body.get("otherFee") != null ? new BigDecimal(body.get("otherFee").toString()) : BigDecimal.ZERO;
        String paymentMethod = (String) body.get("paymentMethod");
        return Result.success(checkInService.checkOut(id, userId, otherFee, paymentMethod));
    }

    @PutMapping("/pay/{id}")
    public Result recordPayment(@PathVariable Long id,
                                @RequestBody Map<String, Object> body) {
        BigDecimal paidAmount = new BigDecimal(body.get("paidAmount").toString());
        String paymentMethod = (String) body.get("paymentMethod");
        return Result.success(checkInService.recordPayment(id, paidAmount, paymentMethod));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.success(checkInService.removeById(id));
    }

    // === 用户端预订 ===

    @PostMapping("/booking")
    public Result submitBooking(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String visitorName = (String) params.get("visitorName");
        String phone = (String) params.get("phone");
        String idCard = (String) params.get("idCard");

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

    // === 用户端退房申请 ===
    // 用户提交退房申请（不自己设定费用）
    @PostMapping("/checkout-request/{id}")
    public Result submitCheckoutRequest(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.submitCheckoutRequest(id, userId));
    }

    // 用户付款
    @PostMapping("/user-pay/{id}")
    public Result userPay(@PathVariable Long id,
                          @RequestBody Map<String, Object> body,
                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        CheckIn record = checkInService.getById(id);
        if (record == null || !userId.equals(record.getCheckInUserId())) {
            return Result.error(403, "无权操作");
        }
        BigDecimal paidAmount = body.get("paidAmount") != null
                ? new BigDecimal(body.get("paidAmount").toString())
                : record.getRoomFee();
        String paymentMethod = (String) body.get("paymentMethod");
        return Result.success(checkInService.recordPayment(id, paidAmount, paymentMethod));
    }

    @GetMapping("/my-bookings")
    public Result getMyBookings(@RequestParam(defaultValue = "1") Long current,
                                @RequestParam(defaultValue = "10") Long size,
                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.selectMyBookings(current, size, userId));
    }

    // === 管理端审批 ===

    @GetMapping("/pending")
    public Result getPending(@RequestParam(defaultValue = "1") Long current,
                              @RequestParam(defaultValue = "10") Long size,
                              @RequestParam(required = false) Integer status) {
        return Result.success(checkInService.selectPendingCheckIns(current, size, status));
    }

    @PutMapping("/approve/{id}")
    public Result approve(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.approveBooking(id, userId));
    }

    @PutMapping("/reject/{id}")
    public Result reject(@PathVariable Long id,
                         @RequestParam(required = false) String reason,
                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.rejectBooking(id, userId, reason));
    }

    // === 管理端退房审批 ===
    // 审核通过退房申请，设置额外费用，完成退房
    @PutMapping("/checkout-approve/{id}")
    public Result approveCheckout(@PathVariable Long id,
                                  @RequestBody Map<String, Object> body,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BigDecimal otherFee = body.get("otherFee") != null ? new BigDecimal(body.get("otherFee").toString()) : BigDecimal.ZERO;
        String paymentMethod = (String) body.get("paymentMethod");
        return Result.success(checkInService.approveCheckoutRequest(id, userId, otherFee, paymentMethod));
    }

    // 审核拒绝退房申请，退房申请被取消，入住状态恢复为已入住
    @PutMapping("/checkout-reject/{id}")
    public Result rejectCheckout(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.rejectCheckoutRequest(id, userId));
    }
}
