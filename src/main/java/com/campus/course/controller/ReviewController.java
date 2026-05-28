package com.campus.course.controller;

import com.campus.course.common.Result;
import com.campus.course.dto.ReviewCreateDTO;
import com.campus.course.model.entity.Review;
import com.campus.course.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 评价接口
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** 提交评价 */
    @PostMapping
    public Result<Review> create(@RequestHeader("X-User-Id") Long userId,
                                 @Validated @RequestBody ReviewCreateDTO dto) {
        return Result.success(reviewService.createReview(userId, dto));
    }

    /** 查看某用户的评价 */
    @GetMapping("/user/{userId}")
    public Result<Page<Review>> listByUser(@PathVariable Long userId,
                                           @RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(reviewService.listByUser(userId, pageNum, pageSize));
    }

    /** 查看某订单的评价 */
    @GetMapping("/order/{orderId}")
    public Result<Page<Review>> listByOrder(@PathVariable Long orderId,
                                            @RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(reviewService.listByOrder(orderId, pageNum, pageSize));
    }
}
