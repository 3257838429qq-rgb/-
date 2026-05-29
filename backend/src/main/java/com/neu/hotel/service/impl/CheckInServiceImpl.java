package com.neu.hotel.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.common.utils.PageUtil;
import com.neu.hotel.entity.CheckIn;
import com.neu.hotel.entity.DormRoom;
import com.neu.hotel.entity.Notification;
import com.neu.hotel.entity.User;
import com.neu.hotel.entity.Visitor;
import com.neu.hotel.entity.VipMember;
import com.neu.hotel.mapper.CheckInMapper;
import com.neu.hotel.mapper.DormRoomMapper;
import com.neu.hotel.mapper.NotificationMapper;
import com.neu.hotel.mapper.UserMapper;
import com.neu.hotel.mapper.VisitorMapper;
import com.neu.hotel.mapper.VipMemberMapper;
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

/**
 * 入住管理服务实现类
 *
 * 【功能说明】
 * - 处理入住、退房、预订的完整业务流程
 * - 管理房间状态（空闲/入住/维护/停用）
 * - 计算房费并支持VIP折扣
 * - 发送系统通知
 *
 * 【业务状态说明】
 * - 入住状态(checkInStatus): 0=待审核, 1=已入住, 2=已退房, 3=已拒绝, 4=退房申请中
 * - 支付状态(paymentStatus): 0=未支付, 1=已支付
 * - 房间状态(room.status): 1=空闲, 2=入住, 3=维护, 4=停用
 *
 * 【关联关系】
 * - 依赖 CheckInMapper、DormRoomMapper、VisitorMapper 等 Mapper
 * - 依赖 NotificationMapper 发送系统通知
 * - 依赖 VipMemberMapper 应用VIP折扣
 * - 被 CheckInController 调用
 *
 * 【对应前端】
 * - API：@/api/dorm/checkin.js
 * - Vue组件：@/views/dorm/checkin/index.vue
 */
@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements CheckInService {

    private final DormRoomMapper dormRoomMapper;
    private final VisitorMapper visitorMapper;
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final VipMemberMapper vipMemberMapper;

    public CheckInServiceImpl(DormRoomMapper dormRoomMapper, VisitorMapper visitorMapper,
                              NotificationMapper notificationMapper, UserMapper userMapper,
                              VipMemberMapper vipMemberMapper) {
        this.dormRoomMapper = dormRoomMapper;
        this.visitorMapper = visitorMapper;
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
        this.vipMemberMapper = vipMemberMapper;
    }

    /**
     * 分页查询入住记录
     */
    @Override
    public IPage<CheckIn> selectPage(Long current, Long size, Map<String, Object> params) {
        Page<CheckIn> page = PageUtil.buildPage(current, size);
        return baseMapper.selectCheckInPage(page, params);
    }

    /**
     * 查询当前入住中的记录
     */
    @Override
    public IPage<CheckIn> selectActiveCheckIns(Long current, Long size) {
        Page<CheckIn> page = PageUtil.buildPage(current, size);
        return baseMapper.selectActiveCheckIns(page);
    }

    /**
     * 查询待审批的入住记录
     */
    @Override
    public IPage<CheckIn> selectPendingCheckIns(Long current, Long size, Integer status) {
        Page<CheckIn> page = PageUtil.buildPage(current, size);
        return baseMapper.selectPendingCheckIns(page, status);
    }

    /**
     * 查询用户的预订记录
     */
    @Override
    public IPage<CheckIn> selectMyBookings(Long current, Long size, Long userId) {
        Page<CheckIn> page = PageUtil.buildPage(current, size);
        return baseMapper.selectMyBookings(page, userId);
    }

    /**
     * 用户提交预订申请（待审核）
     * - 生成订单号
     * - 计算入住天数和房费（应用VIP折扣）
     * - 发送通知给管理员
     */
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
        // 生成16位订单号
        record.setOrderNo(IdUtil.simpleUUID().substring(0, 16).toUpperCase());
        record.setRoomNo(room.getRoomNo());
        record.setCheckInStatus(0);  // 待审核
        record.setCheckInUserId(userId);
        record.setCheckInTime(LocalDateTime.now());
        record.setPaymentStatus(0);   // 未支付

        // 计算入住天数
        long nights = ChronoUnit.DAYS.between(record.getCheckInDate(), record.getCheckOutDate());
        if (nights < 1) {
            nights = 1;
        }
        record.setNights((int) nights);

        // 计算房费并应用VIP折扣
        BigDecimal roomFee = room.getPrice().multiply(BigDecimal.valueOf(nights));
        roomFee = applyVipDiscount(roomFee, userId);
        record.setRoomFee(roomFee);

        boolean saved = baseMapper.insert(record) > 0;

        if (saved) {
            // 发送通知给所有管理员
            sendNotificationToAdmins(
                "新的住宿预订申请",
                "用户「" + (visitor.getName() != null ? visitor.getName() : "") + "」提交了房间「" + room.getRoomNo() + "」的住宿预订，请及时审核。",
                "booking_request",
                record.getId()
            );
        }

        return saved;
    }

    /**
     * 审批通过预订申请
     * - 验证房间可用
     * - 更新入住状态为已入住
     * - 更新房间状态为已入住
     * - 发送通知给用户
     */
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

        record.setCheckInStatus(1);  // 已入住
        record.setCheckInTime(LocalDateTime.now());

        boolean updated = baseMapper.updateById(record) > 0;
        if (updated) {
            // 更新房间状态为已入住
            room.setStatus(2);
            dormRoomMapper.updateById(room);

            // 发送通知给用户
            sendNotification(record.getCheckInUserId(),
                "住宿预订审核通过",
                "您申请的房间「" + record.getRoomNo() + "」（入住：" + record.getCheckInDate() + "）已审核通过，请按期入住。",
                "booking_approved",
                checkInId);
        }
        return updated;
    }

    /**
     * 审批拒绝预订申请
     */
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

        record.setCheckInStatus(3);  // 已拒绝
        record.setRemark(reason != null ? reason : "审核拒绝");
        record.setCheckOutUserId(userId);
        record.setCheckOutTime(LocalDateTime.now());

        boolean updated = baseMapper.updateById(record) > 0;

        if (updated) {
            sendNotification(record.getCheckInUserId(),
                "住宿预订审核未通过",
                "您申请的房间「" + record.getRoomNo() + "」住宿预订未通过审核"
                + (reason != null && !reason.isEmpty() ? "，原因：" + reason : ""),
                "booking_rejected",
                checkInId);
        }

        return updated;
    }

    /**
     * 前台直接办理入住（无需审核）
     * - 直接将房间状态设为已入住
     * - 适用于前台快速登记入住
     */
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
        record.setCheckInStatus(1);  // 已入住
        record.setCheckInUserId(userId);
        record.setCheckInTime(LocalDateTime.now());
        record.setPaymentStatus(0);

        // 计算入住天数和房费
        long nights = ChronoUnit.DAYS.between(record.getCheckInDate(), record.getCheckOutDate());
        if (nights < 1) {
            nights = 1;
        }
        record.setNights((int) nights);
        record.setRoomFee(room.getPrice().multiply(BigDecimal.valueOf(nights)));

        boolean saved = baseMapper.insert(record) > 0;
        if (saved) {
            room.setStatus(2);  // 更新房间状态为已入住
            dormRoomMapper.updateById(room);
        }
        return saved;
    }

    /**
     * 用户提交退房申请
     * - 将状态改为退房申请中，等待管理员审核
     * - 发送通知给管理员
     */
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
        record.setCheckInStatus(4);  // 退房申请中
        boolean updated = baseMapper.updateById(record) > 0;

        if (updated) {
            sendNotificationToAdmins(
                "新的退房申请",
                "用户申请退房，订单号：「" + record.getOrderNo() + "」，房间：「" + record.getRoomNo() + "」，请及时处理。",
                "checkout_request",
                checkInId
            );
        }

        return updated;
    }

    /**
     * 审批通过退房申请
     * - 设置额外费用
     * - 计算总费用并标记为已支付
     * - 更新房间状态为空闲
     */
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
        record.setTotalFee(record.getRoomFee().add(other));  // 总费用 = 房费 + 额外费用
        record.setPaidAmount(record.getTotalFee());         // 已支付金额 = 总费用
        record.setPaymentMethod(paymentMethod != null ? paymentMethod : "现金");
        record.setPaymentStatus(1);   // 已支付
        record.setCheckInStatus(2);   // 已退房
        record.setCheckOutUserId(userId);
        record.setCheckOutTime(LocalDateTime.now());

        boolean updated = baseMapper.updateById(record) > 0;
        if (updated) {
            // 更新房间状态为空闲
            DormRoom room = dormRoomMapper.selectById(record.getRoomId());
            if (room != null) {
                room.setStatus(1);
                dormRoomMapper.updateById(room);
            }

            sendNotification(record.getCheckInUserId(),
                "退房申请已通过",
                "您的退房申请已审核通过，订单号：「" + record.getOrderNo() + "」，房间：「" + record.getRoomNo()
                + "」，合计费用：" + record.getTotalFee() + "元。",
                "checkout_approved",
                checkInId);
        }
        return updated;
    }

    /**
     * 审批拒绝退房申请
     * - 恢复入住状态为已入住
     */
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
        record.setCheckInStatus(1);  // 恢复为已入住
        boolean updated = baseMapper.updateById(record) > 0;

        if (updated) {
            sendNotification(record.getCheckInUserId(),
                "退房申请未通过",
                "您的退房申请未通过审核，订单号：「" + record.getOrderNo() + "」，请继续正常使用房间。",
                "checkout_rejected",
                checkInId);
        }

        return updated;
    }

    /**
     * 前台直接办理退房
     * - 设置额外费用和支付信息
     * - 标记为已支付
     * - 更新房间状态为空闲
     */
    @Override
    @Transactional
    public boolean checkOut(Long checkInId, Long userId, BigDecimal otherFee, String paymentMethod) {
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
                room.setStatus(1);  // 更新房间状态为空闲
                dormRoomMapper.updateById(room);
            }
        }
        return updated;
    }

    /**
     * 记录支付信息
     * - 支持余额支付（VIP余额）
     * - 更新支付状态
     */
    @Override
    @Transactional
    public boolean recordPayment(Long checkInId, BigDecimal paidAmount, String paymentMethod) {
        CheckIn record = baseMapper.selectById(checkInId);
        if (record == null) {
            throw new RuntimeException("入住记录不存在");
        }
        BigDecimal amount = paidAmount != null ? paidAmount : record.getRoomFee();
        record.setPaidAmount(amount);
        record.setPaymentMethod(paymentMethod);
        record.setPaymentStatus(1);

        // 余额支付：扣减VIP余额
        if ("余额支付".equals(paymentMethod)) {
            LambdaQueryWrapper<VipMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(VipMember::getUserId, record.getCheckInUserId());
            VipMember member = vipMemberMapper.selectOne(wrapper);
            if (member == null || member.getBalance() == null || member.getBalance().compareTo(amount) < 0) {
                throw new RuntimeException("余额不足，请选择其他支付方式");
            }
            member.setBalance(member.getBalance().subtract(amount));
            vipMemberMapper.updateById(member);
        }

        return baseMapper.updateById(record) > 0;
    }

    /**
     * 获取入住统计数据
     * 用于仪表盘展示
     */
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 当前入住数（已入住 + 退房申请中）
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CheckIn::getCheckInStatus, 1, 4);
        long activeCount = baseMapper.selectCount(wrapper);
        stats.put("activeCheckIns", activeCount);

        // 空闲房间数
        LambdaQueryWrapper<DormRoom> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(DormRoom::getStatus, 1);
        long availableRooms = dormRoomMapper.selectCount(roomWrapper);
        stats.put("availableRooms", availableRooms);

        // 总房间数
        LambdaQueryWrapper<DormRoom> totalWrapper = new LambdaQueryWrapper<>();
        long totalRooms = dormRoomMapper.selectCount(totalWrapper);
        stats.put("totalRooms", totalRooms);
        stats.put("occupancyRate", totalRooms > 0 ? (totalRooms - availableRooms) * 100.0 / totalRooms : 0);

        // 待审核预订数
        LambdaQueryWrapper<CheckIn> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(CheckIn::getCheckInStatus, 0);
        long pendingBookings = baseMapper.selectCount(pendingWrapper);
        stats.put("pendingBookings", pendingBookings);

        // 待处理退房数
        LambdaQueryWrapper<CheckIn> checkoutReqWrapper = new LambdaQueryWrapper<>();
        checkoutReqWrapper.eq(CheckIn::getCheckInStatus, 4);
        long pendingCheckouts = baseMapper.selectCount(checkoutReqWrapper);
        stats.put("pendingCheckouts", pendingCheckouts);

        // 待审核访客数
        LambdaQueryWrapper<Visitor> visitorPendingWrapper = new LambdaQueryWrapper<>();
        visitorPendingWrapper.eq(Visitor::getStatus, 0);
        long pendingVisitors = visitorMapper.selectCount(visitorPendingWrapper);
        stats.put("pendingVisitors", pendingVisitors);

        // 本月收入
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

    /**
     * 发送通知给指定用户
     */
    private void sendNotification(Long userId, String title, String content, String type, Long businessId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setBusinessId(businessId);
        notification.setReadStatus(0);
        notificationMapper.insert(notification);
    }

    /**
     * 发送通知给所有管理员
     */
    private void sendNotificationToAdmins(String title, String content, String type, Long businessId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserType, 1);   // userType=1 表示管理员
        wrapper.eq(User::getStatus, 1);
        List<User> admins = userMapper.selectList(wrapper);
        for (User admin : admins) {
            sendNotification(admin.getId(), title, content, type, businessId);
        }
    }

    /**
     * 应用VIP折扣
     * @param price 原价
     * @param userId 用户ID
     * @return 折后价
     */
    private BigDecimal applyVipDiscount(BigDecimal price, Long userId) {
        if (userId == null) return price;
        LambdaQueryWrapper<VipMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VipMember::getUserId, userId);
        VipMember member = vipMemberMapper.selectOne(wrapper);
        if (member != null && member.isActive() && member.getDiscountRate() != null) {
            return price.multiply(member.getDiscountRate()).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return price;
    }
}
