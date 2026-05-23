package com.neu.hotel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.hotel.entity.CheckIn;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface CheckInService extends IService<CheckIn> {

    IPage<CheckIn> selectPage(Long current, Long size, Map<String, Object> params);

    IPage<CheckIn> selectActiveCheckIns(Long current, Long size);

    IPage<CheckIn> selectPendingCheckIns(Long current, Long size);

    IPage<CheckIn> selectMyBookings(Long current, Long size, Long userId);

    boolean checkIn(CheckIn checkIn, Long userId);

    boolean submitBooking(CheckIn checkIn, Long userId);

    boolean approveBooking(Long checkInId, Long userId);

    boolean rejectBooking(Long checkInId, Long userId, String reason);

    boolean checkOut(Long checkInId, Long userId, BigDecimal otherFee, String paymentMethod);

    boolean recordPayment(Long checkInId, BigDecimal paidAmount, String paymentMethod);

    Map<String, Object> getStatistics();
}
