package com.neu.hotel.controller;

import com.neu.hotel.common.annotation.Log;
import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.Review;
import com.neu.hotel.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评价管理控制器
 *
 * 【功能说明】
 * - 处理客人入住后的评价
 * - 管理评价回复
 * - 提供评价查询功能
 *
 * 【API前缀】/review
 *
 * 【主要接口】
 * 1. 提交评价 - POST /review
 * 2. 我的评价 - GET /review/my
 * 3. 入住记录的评价 - GET /review/checkin/{checkInId}
 * 4. 最新评价列表 - GET /review/recent
 * 5. 房间评价列表 - GET /review/room/{roomId}
 * 6. 回复评价 - PUT /review/reply/{id}
 *
 * 【关联关系】
 * - 依赖 ReviewService 处理评价业务
 * - 关联 CheckIn 实体（通过 checkInId）
 * - 关联 DormRoom 实体（通过 roomId）
 *
 * 【对应前端】
 * - 路由：/admin/review
 * - 路由：/portal/review
 * - API：@/api/review.js
 * - Vue组件：@/views/visitor/review/index.vue
 */
@RestController
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 用户提交评价
     * POST /review
     * body: { checkInId, rating, content }
     */
    @PostMapping
    @Log(title = "评价管理", businessType = "INSERT", operType = "提交评价")
    public Result submit(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long checkInId = Long.valueOf(body.get("checkInId").toString());
        Integer rating = Integer.valueOf(body.get("rating").toString());
        String content = (String) body.get("content");
        return Result.success(reviewService.submitReview(checkInId, userId, rating, content));
    }

    /**
     * 获取当前用户的评价（针对某次入住）
     * GET /review/my?checkInId=xxx
     */
    @GetMapping("/my")
    public Result getMyReview(@RequestParam Long checkInId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(reviewService.getMyReview(checkInId, userId));
    }

    /**
     * 获取某次入住的评价
     * GET /review/checkin/{checkInId}
     */
    @GetMapping("/checkin/{checkInId}")
    public Result getCheckInReview(@PathVariable Long checkInId) {
        return Result.success(reviewService.getCheckInReview(checkInId));
    }

    /**
     * 获取最新的评价列表（用于首页展示）
     * GET /review/recent?limit=6
     */
    @GetMapping("/recent")
    public Result getRecentReviews(@RequestParam(defaultValue = "6") Integer limit) {
        return Result.success(reviewService.getRecentReviews(limit));
    }

    /**
     * 获取某个房间的所有评价
     * GET /review/room/{roomId}
     */
    @GetMapping("/room/{roomId}")
    public Result getRoomReviews(@PathVariable Long roomId) {
        return Result.success(reviewService.getRoomReviews(roomId));
    }

    /**
     * 管理员回复评价
     * PUT /review/reply/{id}?reply=xxx
     */
    @PutMapping("/reply/{id}")
    @Log(title = "评价管理", businessType = "UPDATE", operType = "回复评价")
    public Result reply(@PathVariable Long id,
                        @RequestParam String reply,
                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(reviewService.adminReply(id, userId, reply));
    }
}
