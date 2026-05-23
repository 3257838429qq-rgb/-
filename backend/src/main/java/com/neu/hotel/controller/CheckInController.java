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
                           @RequestParam(required = false) BigDecimal otherFee,
                           @RequestParam(required = false) String paymentMethod,
                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.checkOut(id, userId, otherFee, paymentMethod));
    }

    @PutMapping("/pay/{id}")
    public Result recordPayment(@PathVariable Long id,
                                @RequestParam BigDecimal paidAmount,
                                @RequestParam String paymentMethod,
                                HttpServletRequest request) {
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
                              @RequestParam(defaultValue = "10") Long size) {
        return Result.success(checkInService.selectPendingCheckIns(current, size));
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
}
