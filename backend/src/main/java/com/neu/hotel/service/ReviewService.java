package com.neu.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.hotel.entity.Review;
import java.util.List;
import java.util.Map;

public interface ReviewService extends IService<Review> {

    boolean submitReview(Long checkInId, Long userId, Integer rating, String content);

    Map<String, Object> getMyReview(Long checkInId, Long userId);

    Map<String, Object> getCheckInReview(Long checkInId);

    boolean adminReply(Long reviewId, Long userId, String reply);

    List<Map<String, Object>> getRecentReviews(Integer limit);

    List<Map<String, Object>> getRoomReviews(Long roomId);
}
