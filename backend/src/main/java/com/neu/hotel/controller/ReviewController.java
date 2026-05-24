package com.neu.hotel.controller;

import com.neu.hotel.common.result.Result;
import com.neu.hotel.entity.Review;
import com.neu.hotel.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Result submit(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long checkInId = Long.valueOf(body.get("checkInId").toString());
        Integer rating = Integer.valueOf(body.get("rating").toString());
        String content = (String) body.get("content");
        return Result.success(reviewService.submitReview(checkInId, userId, rating, content));
    }

    @GetMapping("/my")
    public Result getMyReview(@RequestParam Long checkInId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(reviewService.getMyReview(checkInId, userId));
    }

    @GetMapping("/checkin/{checkInId}")
    public Result getCheckInReview(@PathVariable Long checkInId) {
        return Result.success(reviewService.getCheckInReview(checkInId));
    }

    @GetMapping("/recent")
    public Result getRecentReviews(@RequestParam(defaultValue = "6") Integer limit) {
        return Result.success(reviewService.getRecentReviews(limit));
    }

    @GetMapping("/room/{roomId}")
    public Result getRoomReviews(@PathVariable Long roomId) {
        return Result.success(reviewService.getRoomReviews(roomId));
    }

    @PutMapping("/reply/{id}")
    public Result reply(@PathVariable Long id,
                        @RequestParam String reply,
                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(reviewService.adminReply(id, userId, reply));
    }
}
