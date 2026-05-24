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

    @Override
    public boolean submitReview(Long checkInId, Long userId, Integer rating, String content) {
        CheckIn checkIn = checkInMapper.selectById(checkInId);
        if (checkIn == null) {
            throw new RuntimeException("订单不存在");
        }
        if (checkIn.getCheckInStatus() != 2) {
            throw new RuntimeException("只能对已退房订单进行评价");
        }

        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getCheckInId, checkInId).eq(Review::getUserId, userId);
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("您已评价过该订单");
        }

        Review review = new Review();
        review.setCheckInId(checkInId);
        review.setUserId(userId);
        review.setRoomId(checkIn.getRoomId());
        review.setRating(rating);
        review.setContent(content);
        return baseMapper.insert(review) > 0;
    }

    @Override
    public Map<String, Object> getMyReview(Long checkInId, Long userId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getCheckInId, checkInId).eq(Review::getUserId, userId);
        Review review = baseMapper.selectOne(wrapper);
        if (review == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", review.getId());
        map.put("rating", review.getRating());
        map.put("content", review.getContent());
        map.put("reply", review.getReply());
        map.put("replyTime", review.getReplyTime());
        map.put("createTime", review.getCreateTime());
        return map;
    }

    @Override
    public Map<String, Object> getCheckInReview(Long checkInId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getCheckInId, checkInId);
        Review review = baseMapper.selectOne(wrapper);
        if (review == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", review.getId());
        map.put("rating", review.getRating());
        map.put("content", review.getContent());
        map.put("reply", review.getReply());
        map.put("replyTime", review.getReplyTime());
        map.put("createTime", review.getCreateTime());
        return map;
    }

    @Override
    public List<Map<String, Object>> getRecentReviews(Integer limit) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Review::getCreateTime);
        wrapper.last("LIMIT " + limit);
        List<Review> reviews = baseMapper.selectList(wrapper);
        return buildReviewList(reviews);
    }

    @Override
    public List<Map<String, Object>> getRoomReviews(Long roomId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getRoomId, roomId);
        wrapper.orderByDesc(Review::getCreateTime);
        List<Review> reviews = baseMapper.selectList(wrapper);
        return buildReviewList(reviews);
    }

    private List<Map<String, Object>> buildReviewList(List<Review> reviews) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Review r : reviews) {
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

            DormRoom room = dormRoomMapper.selectById(r.getRoomId());
            map.put("roomNo", room != null ? room.getRoomNo() : "");

            User user = userMapper.selectById(r.getUserId());
            map.put("userName", user != null ? user.getRealName() : "匿名用户");

            result.add(map);
        }
        return result;
    }

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
