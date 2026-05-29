package com.neu.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.common.utils.PageUtil;
import com.neu.hotel.entity.Notification;
import com.neu.hotel.entity.Visitor;
import com.neu.hotel.mapper.NotificationMapper;
import com.neu.hotel.mapper.VisitorMapper;
import com.neu.hotel.service.VisitorService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 访客管理服务实现类
 *
 * 【功能说明】
 * - 处理访客预约的审核流程
 * - 管理访客来访状态
 * - 发送审核结果通知
 *
 * 【访客状态说明】
 * - status=0: 待审核
 * - status=1: 已通过
 * - status=2: 已拒绝
 *
 * 【关联关系】
 * - 依赖 VisitorMapper 操作访客数据
 * - 依赖 NotificationMapper 发送通知
 * - 被 VisitorController 调用
 * - 关联 Dept 实体（通过 hostDeptId）
 *
 * 【对应前端】
 * - API：@/api/visitor/index.js
 * - Vue组件：@/views/visitor/list/index.vue, @/views/portal/Visit.vue
 */
@Service
public class VisitorServiceImpl extends ServiceImpl<VisitorMapper, Visitor> implements VisitorService {

    private final NotificationMapper notificationMapper;

    public VisitorServiceImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    /**
     * 分页查询访客列表（通用方法）
     */
    @Override
    public IPage<Visitor> selectPage(Long current, Long size, Map<String, Object> params) {
        Page<Visitor> page = PageUtil.buildPage(current, size);
        return baseMapper.selectVisitorPage(page, params);
    }

    /**
     * 审核访客申请
     * - 更新审核状态
     * - 记录审核人和审核时间
     * - 发送通知给申请人
     * @param visitorId 访客ID
     * @param status 审核状态(1=通过, 2=拒绝)
     * @param reviewerId 审核人ID
     * @param remark 审核备注
     * @return 是否成功
     */
    @Override
    public boolean review(Long visitorId, Integer status, Long reviewerId, String remark) {
        Visitor visitor = baseMapper.selectById(visitorId);
        if (visitor == null) {
            throw new RuntimeException("访客记录不存在");
        }
        visitor.setStatus(status);
        visitor.setReviewerId(reviewerId);
        visitor.setReviewRemark(remark);
        visitor.setReviewTime(LocalDateTime.now());

        boolean updated = baseMapper.updateById(visitor) > 0;

        // 发送通知给提交人
        if (updated && visitor.getCreateUserId() != null) {
            String title = status == 1 ? "访客预约审核通过" : "访客预约审核结果";
            String content = status == 1
                    ? "您提交的访客「" + visitor.getName() + "」来访预约已审核通过。"
                    : "您提交的访客「" + visitor.getName() + "」来访预约未通过审核"
                      + (remark != null && !remark.isEmpty() ? "，原因：" + remark : "");
            sendNotification(visitor.getCreateUserId(), title, content, status == 1 ? "visitor_approved" : "visitor_rejected", visitorId);
        }

        return updated;
    }

    /**
     * 发送系统通知
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
     * 查询待审核的访客列表
     */
    @Override
    public IPage<Visitor> selectPendingVisitors(Long current, Long size) {
        Page<Visitor> page = PageUtil.buildPage(current, size);
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Visitor::getStatus, 0);  // 待审核
        wrapper.orderByDesc(Visitor::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 查询已审核通过的访客列表
     */
    @Override
    public IPage<Visitor> selectApprovedVisitors(Long current, Long size) {
        Page<Visitor> page = PageUtil.buildPage(current, size);
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Visitor::getStatus, 1);  // 已通过
        wrapper.orderByDesc(Visitor::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 多条件查询访客列表
     * 支持按姓名、手机号、状态筛选
     */
    @Override
    public IPage<Visitor> selectAllVisitors(Long current, Long size, String name, String phone, Integer status) {
        Page<Visitor> page = PageUtil.buildPage(current, size);
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        // 按姓名模糊查询
        if (StringUtils.isNotBlank(name)) {
            wrapper.like(Visitor::getName, name);
        }
        // 按手机号模糊查询
        if (StringUtils.isNotBlank(phone)) {
            wrapper.like(Visitor::getPhone, phone);
        }
        // 按状态精确查询
        if (status != null) {
            wrapper.eq(Visitor::getStatus, status);
        }
        wrapper.orderByDesc(Visitor::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
}
