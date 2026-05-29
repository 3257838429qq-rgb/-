package com.neu.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.hotel.entity.CheckIn;
import com.neu.hotel.entity.DormRoom;
import com.neu.hotel.entity.Review;
import com.neu.hotel.entity.User;
import com.neu.hotel.mapper.CheckInMapper;
import com.neu.hotel.mapper.DormRoomMapper;
import com.neu.hotel.mapper.ReviewMapper;
import com.neu.hotel.mapper.UserMapper;
import com.neu.hotel.service.ReviewService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价管理服务实现类
 *
 * 【功能说明】
 * - 处理用户提交入住评价
 * - 管理评价回复
 * - 提供评价查询功能
 *
 * 【业务规则】
 * - 只能对已退房(status=2)的订单进行评价
 * - 每个订单每个用户只能评价一次
 * - 管理员可以回复评价
 *
 * 【关联关系】
 * - 依赖 ReviewMapper 操作评价数据
 * - 依赖 CheckInMapper 验证订单状态
 * - 依赖 DormRoomMapper 获取房间信息
 * - 依赖 UserMapper 获取用户信息
 * - 被 ReviewController 调用
 *
 * 【对应前端】
 * - API：@/api/review.js
 * - Vue组件：@/views/visitor/review/index.vue
 */
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    private final CheckInMapper checkInMapper;
    private final DormRoomMapper dormRoomMapper;
    private final UserMapper userMapper;

    public ReviewServiceImpl(CheckInMapper checkInMapper, DormRoomMapper dormRoomMapper, UserMapper userMapper) {
        this.checkInMapper = checkInMapper;
        this.dormRoomMapper = dormRoomMapper;
        this.userMapper = userMapper;
    }

    /**
     * 提交评价
     * - 验证订单存在且已退房
     * - 检查是否已评价（防止重复评价）
     * - 创建评价记录
     * @param checkInId 订单ID
     * @param userId 用户ID
     * @param rating 评分（1-5星）
     * @param content 评价内容
     * @return 是否成功
     */
    @Override
    public boolean submitReview(Long checkInId, Long userId, Integer rating, String content) {
        CheckIn checkIn = checkInMapper.selectById(checkInId);
        if (checkIn == null) {
            throw new RuntimeException("订单不存在");
        }
        if (checkIn.getCheckInStatus() != 2) {
            throw new RuntimeException("只能对已退房订单进行评价");
        }

        // 检查是否已评价
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getCheckInId, checkInId).eq(Review::getUserId, userId);
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("您已评价过该订单");
        }

        // 创建评价记录
        Review review = new Review();
        review.setCheckInId(checkInId);
        review.setUserId(userId);
        review.setRoomId(checkIn.getRoomId());
        review.setRating(rating);
        review.setContent(content);
        return baseMapper.insert(review) > 0;
    }

    /**
     * 获取用户对某订单的评价
     * @param checkInId 订单ID
     * @param userId 用户ID
     * @return 评价信息
     */
    @Override
    public Map<String, Object> getMyReview(Long checkInId, Long userId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getCheckInId, checkInId).eq(Review::getUserId, userId);
        Review review = baseMapper.selectOne(wrapper);
        if (review == null) {
            return null;
        }
        return buildReviewMap(review);
    }

    /**
     * 获取某订单的评价
     * @param checkInId 订单ID
     * @return 评价信息
     */
    @Override
    public Map<String, Object> getCheckInReview(Long checkInId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getCheckInId, checkInId);
        Review review = baseMapper.selectOne(wrapper);
        if (review == null) {
            return null;
        }
        return buildReviewMap(review);
    }

    /**
     * 获取最新评价列表
     * 用于首页展示最新评价
     * @param limit 返回数量
     * @return 评价列表
     */
    @Override
    public List<Map<String, Object>> getRecentReviews(Integer limit) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Review::getCreateTime);
        wrapper.last("LIMIT " + limit);
        List<Review> reviews = baseMapper.selectList(wrapper);
        return buildReviewList(reviews);
    }

    /**
     * 获取某房间的所有评价
     * @param roomId 房间ID
     * @return 评价列表
     */
    @Override
    public List<Map<String, Object>> getRoomReviews(Long roomId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getRoomId, roomId);
        wrapper.orderByDesc(Review::getCreateTime);
        List<Review> reviews = baseMapper.selectList(wrapper);
        return buildReviewList(reviews);
    }

    /**
     * 构建评价列表（包含房间号和用户名）
     * @param reviews 评价原始列表
     * @return 增强后的评价列表
     */
    private List<Map<String, Object>> buildReviewList(List<Review> reviews) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Review r : reviews) {
            Map<String, Object> map = buildReviewMap(r);

            // 关联查询房间号
            DormRoom room = dormRoomMapper.selectById(r.getRoomId());
            map.put("roomNo", room != null ? room.getRoomNo() : "");

            // 关联查询用户名
            User user = userMapper.selectById(r.getUserId());
            map.put("userName", user != null ? user.getRealName() : "匿名用户");

            result.add(map);
        }
        return result;
    }

    /**
     * 构建单个评价Map
     */
    private Map<String, Object> buildReviewMap(Review r) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", r.getId());
        map.put("checkInId", r.getCheckInId());
        map.put("userId", r.getUserId());
        map.put("roomId", r.getRoomId());
        map.put("rating", r.getRating());
        map.put("content", r.getContent());
        map.put("reply", r.getReply());
        map.put("replyTime", r.getReplyTime());
        map.put("createTime", r.getCreateTime());
        return map;
    }

    /**
     * 管理员回复评价
     * @param reviewId 评价ID
     * @param userId 回复人ID（管理员）
     * @param reply 回复内容
     * @return 是否成功
     */
    @Override
    public boolean adminReply(Long reviewId, Long userId, String reply) {
        Review review = baseMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        review.setReply(reply);
        review.setReplyTime(LocalDateTime.now());
        review.setReplyUserId(userId);
        return baseMapper.updateById(review) > 0;
    }
}
