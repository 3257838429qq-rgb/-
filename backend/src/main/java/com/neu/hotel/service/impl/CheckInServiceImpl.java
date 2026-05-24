package com.neu.hotel.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.common.utils.PageUtil;
import com.neu.hotel.entity.CheckIn;
import com.neu.hotel.entity.DormRoom;
import com.neu.hotel.entity.Visitor;
import com.neu.hotel.mapper.CheckInMapper;
import com.neu.hotel.mapper.DormRoomMapper;
import com.neu.hotel.mapper.VisitorMapper;
import com.neu.hotel.service.CheckInService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements CheckInService {

    private final DormRoomMapper dormRoomMapper;
    private final VisitorMapper visitorMapper;

    public CheckInServiceImpl(DormRoomMapper dormRoomMapper, VisitorMapper visitorMapper) {
        this.dormRoomMapper = dormRoomMapper;
        this.visitorMapper = visitorMapper;
    }

    @Override
    public IPage<CheckIn> selectPage(Long current, Long size, Map<String, Object> params) {
        Page<CheckIn> page = PageUtil.buildPage(current, size);
        return baseMapper.selectCheckInPage(page, params);
    }

    @Override
    public IPage<CheckIn> selectActiveCheckIns(Long current, Long size) {
        Page<CheckIn> page = PageUtil.buildPage(current, size);
        return baseMapper.selectActiveCheckIns(page);
    }

    @Override
    public IPage<CheckIn> selectPendingCheckIns(Long current, Long size, Integer status) {
        Page<CheckIn> page = PageUtil.buildPage(current, size);
        return baseMapper.selectPendingCheckIns(page, status);
    }

    @Override
    public IPage<CheckIn> selectMyBookings(Long current, Long size, Long userId) {
        Page<CheckIn> page = PageUtil.buildPage(current, size);
        return baseMapper.selectMyBookings(page, userId);
    }

    @Override
    @Transactional
    public boolean submitBooking(CheckIn checkIn, Long userId) {
        DormRoom room = dormRoomMapper.selectById(checkIn.getRoomId());
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }

        Visitor visitor = visitorMapper.selectById(checkIn.getVisitorId());
        if (visitor == null) {
            throw new RuntimeException("访客信息不存在");
        }

        CheckIn record = new CheckIn();
        BeanUtils.copyProperties(checkIn, record);
        record.setOrderNo(IdUtil.simpleUUID().substring(0, 16).toUpperCase());
        record.setRoomNo(room.getRoomNo());
        record.setCheckInStatus(0); // 待审核
        record.setCheckInUserId(userId);
        record.setCheckInTime(LocalDateTime.now());
        record.setPaymentStatus(0);

        long nights = ChronoUnit.DAYS.between(record.getCheckInDate(), record.getCheckOutDate());
        if (nights < 1) {
            nights = 1;
        }
        record.setNights((int) nights);
        record.setRoomFee(room.getPrice().multiply(BigDecimal.valueOf(nights)));

        return baseMapper.insert(record) > 0;
    }

    @Override
    @Transactional
    public boolean approveBooking(Long checkInId, Long userId) {
        CheckIn record = baseMapper.selectById(checkInId);
        if (record == null) {
            throw new RuntimeException("预订记录不存在");
        }
        if (record.getCheckInStatus() != 0) {
            throw new RuntimeException("该记录不是待审核状态");
        }

        DormRoom room = dormRoomMapper.selectById(record.getRoomId());
        if (room == null || room.getStatus() != 1) {
            throw new RuntimeException("房间不可用");
        }

        record.setCheckInStatus(1);
        record.setCheckInTime(LocalDateTime.now());

        boolean updated = baseMapper.updateById(record) > 0;
        if (updated) {
            room.setStatus(2);
            dormRoomMapper.updateById(room);
        }
        return updated;
    }

    @Override
    @Transactional
    public boolean rejectBooking(Long checkInId, Long userId, String reason) {
        CheckIn record = baseMapper.selectById(checkInId);
        if (record == null) {
            throw new RuntimeException("预订记录不存在");
        }
        if (record.getCheckInStatus() != 0) {
            throw new RuntimeException("该记录不是待审核状态");
        }

        record.setCheckInStatus(3); // 已取消
        record.setRemark(reason != null ? reason : "审核拒绝");
        record.setCheckOutUserId(userId);
        record.setCheckOutTime(LocalDateTime.now());

        return baseMapper.updateById(record) > 0;
    }

    @Override
    @Transactional
    public boolean checkIn(CheckIn checkIn, Long userId) {
        DormRoom room = dormRoomMapper.selectById(checkIn.getRoomId());
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        if (room.getStatus() != 1) {
            throw new RuntimeException("房间不可用");
        }

        Visitor visitor = visitorMapper.selectById(checkIn.getVisitorId());
        if (visitor == null) {
            throw new RuntimeException("访客信息不存在");
        }

        CheckIn record = new CheckIn();
        BeanUtils.copyProperties(checkIn, record);
        record.setOrderNo(IdUtil.simpleUUID().substring(0, 16).toUpperCase());
        record.setRoomNo(room.getRoomNo());
        record.setCheckInStatus(1);
        record.setCheckInUserId(userId);
        record.setCheckInTime(LocalDateTime.now());
        record.setPaymentStatus(0);

        long nights = ChronoUnit.DAYS.between(record.getCheckInDate(), record.getCheckOutDate());
        if (nights < 1) {
            nights = 1;
        }
        record.setNights((int) nights);
        record.setRoomFee(room.getPrice().multiply(BigDecimal.valueOf(nights)));

        boolean saved = baseMapper.insert(record) > 0;
        if (saved) {
            room.setStatus(2);
            dormRoomMapper.updateById(room);
        }
        return saved;
    }

    @Override
    @Transactional
    public boolean submitCheckoutRequest(Long checkInId, Long userId) {
        CheckIn record = baseMapper.selectById(checkInId);
        if (record == null) {
            throw new RuntimeException("入住记录不存在");
        }
        if (record.getCheckInStatus() != 1) {
            throw new RuntimeException("只有入住中的记录可以申请退房");
        }
        if (!userId.equals(record.getCheckInUserId())) {
            throw new RuntimeException("只能申请自己的订单退房");
        }
        // 状态改为4（退房申请中）
        record.setCheckInStatus(4);
        return baseMapper.updateById(record) > 0;
    }

    @Override
    @Transactional
    public boolean approveCheckoutRequest(Long checkInId, Long userId, BigDecimal otherFee, String paymentMethod) {
        CheckIn record = baseMapper.selectById(checkInId);
        if (record == null) {
            throw new RuntimeException("入住记录不存在");
        }
        if (record.getCheckInStatus() != 4) {
            throw new RuntimeException("该记录不是退房申请状态");
        }
        BigDecimal other = otherFee != null ? otherFee : BigDecimal.ZERO;
        record.setOtherFee(other);
        record.setTotalFee(record.getRoomFee().add(other));
        record.setPaidAmount(record.getTotalFee());
        record.setPaymentMethod(paymentMethod != null ? paymentMethod : "现金");
        record.setPaymentStatus(1);
        record.setCheckInStatus(2);
        record.setCheckOutUserId(userId);
        record.setCheckOutTime(LocalDateTime.now());

        boolean updated = baseMapper.updateById(record) > 0;
        if (updated) {
            DormRoom room = dormRoomMapper.selectById(record.getRoomId());
            if (room != null) {
                room.setStatus(1);
                dormRoomMapper.updateById(room);
            }
        }
        return updated;
    }

    @Override
    @Transactional
    public boolean rejectCheckoutRequest(Long checkInId, Long userId) {
        CheckIn record = baseMapper.selectById(checkInId);
        if (record == null) {
            throw new RuntimeException("入住记录不存在");
        }
        if (record.getCheckInStatus() != 4) {
            throw new RuntimeException("该记录不是退房申请状态");
        }
        // 状态恢复为1（入住中）
        record.setCheckInStatus(1);
        return baseMapper.updateById(record) > 0;
    }

    @Override
    @Transactional
    public boolean checkOut(Long checkInId, Long userId, BigDecimal otherFee, String paymentMethod) {
        // 此方法仅供管理员使用，用户退房请使用退房申请接口
        CheckIn record = baseMapper.selectById(checkInId);
        if (record == null) {
            throw new RuntimeException("入住记录不存在");
        }
        if (record.getCheckInStatus() != 1) {
            throw new RuntimeException("该记录已退房或状态异常");
        }

        BigDecimal other = otherFee != null ? otherFee : BigDecimal.ZERO;
        record.setOtherFee(other);
        record.setTotalFee(record.getRoomFee().add(other));
        record.setPaidAmount(record.getTotalFee());
        record.setPaymentMethod(paymentMethod);
        record.setPaymentStatus(1);
        record.setCheckInStatus(2);
        record.setCheckOutUserId(userId);
        record.setCheckOutTime(LocalDateTime.now());

        boolean updated = baseMapper.updateById(record) > 0;
        if (updated) {
            DormRoom room = dormRoomMapper.selectById(record.getRoomId());
            if (room != null) {
                room.setStatus(1);
                dormRoomMapper.updateById(room);
            }
        }
        return updated;
    }

    @Override
    public boolean recordPayment(Long checkInId, BigDecimal paidAmount, String paymentMethod) {
        CheckIn record = baseMapper.selectById(checkInId);
        if (record == null) {
            throw new RuntimeException("入住记录不存在");
        }
        record.setPaidAmount(paidAmount != null ? paidAmount : record.getRoomFee());
        record.setPaymentMethod(paymentMethod);
        record.setPaymentStatus(1);
        return baseMapper.updateById(record) > 0;
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getCheckInStatus, 1);
        long activeCount = baseMapper.selectCount(wrapper);
        stats.put("activeCheckIns", activeCount);

        LambdaQueryWrapper<DormRoom> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(DormRoom::getStatus, 1);
        long availableRooms = dormRoomMapper.selectCount(roomWrapper);
        stats.put("availableRooms", availableRooms);

        LambdaQueryWrapper<DormRoom> totalWrapper = new LambdaQueryWrapper<>();
        long totalRooms = dormRoomMapper.selectCount(totalWrapper);
        stats.put("totalRooms", totalRooms);
        stats.put("occupancyRate", totalRooms > 0 ? (totalRooms - availableRooms) * 100.0 / totalRooms : 0);

        LambdaQueryWrapper<CheckIn> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(CheckIn::getCheckInStatus, 0);
        long pendingBookings = baseMapper.selectCount(pendingWrapper);
        stats.put("pendingBookings", pendingBookings);

        // 退房申请数量
        LambdaQueryWrapper<CheckIn> checkoutReqWrapper = new LambdaQueryWrapper<>();
        checkoutReqWrapper.eq(CheckIn::getCheckInStatus, 4);
        long pendingCheckouts = baseMapper.selectCount(checkoutReqWrapper);
        stats.put("pendingCheckouts", pendingCheckouts);

        LambdaQueryWrapper<Visitor> visitorPendingWrapper = new LambdaQueryWrapper<>();
        visitorPendingWrapper.eq(Visitor::getStatus, 0);
        long pendingVisitors = visitorMapper.selectCount(visitorPendingWrapper);
        stats.put("pendingVisitors", pendingVisitors);

        LambdaQueryWrapper<CheckIn> monthWrapper = new LambdaQueryWrapper<>();
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        monthWrapper.ge(CheckIn::getCheckInDate, monthStart);
        List<CheckIn> monthRecords = baseMapper.selectList(monthWrapper);
        BigDecimal monthIncome = monthRecords.stream()
                .map(CheckIn::getTotalFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("monthIncome", monthIncome);

        return stats;
    }
}
