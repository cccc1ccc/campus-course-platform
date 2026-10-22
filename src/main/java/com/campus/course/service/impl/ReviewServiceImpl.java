package com.campus.course.service.impl;

import com.campus.course.common.BizException;
import com.campus.course.common.ErrorCode;
import com.campus.course.dto.ReviewCreateDTO;
import com.campus.course.model.entity.CourseOrder;
import com.campus.course.model.entity.Notification;
import com.campus.course.model.entity.Review;
import com.campus.course.model.enum_.OrderStatus;
import com.campus.course.repository.CourseOrderRepository;
import com.campus.course.repository.NotificationRepository;
import com.campus.course.repository.ReviewRepository;
import com.campus.course.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepo;
    private final CourseOrderRepository orderRepo;
    private final NotificationRepository notifyRepo;

    @Override
    @Transactional
    public Review createReview(Long reviewerId, ReviewCreateDTO dto) {
        CourseOrder order = orderRepo.findById(dto.getOrderId())
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND));

        // 只有已完成的订单才能评价
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BizException(ErrorCode.REVIEW_NOT_ALLOWED,
                    "订单状态为【" + order.getStatus().getDesc() + "】，无法评价");
        }

        // 防止重复评价
        if (reviewRepo.existsByOrderId(order.getId())) {
            throw new BizException(ErrorCode.REVIEW_DUPLICATED);
        }

        // 确定被评价者（发布者评价接单者，反之亦然）
        Long revieweeId;
        if (reviewerId.equals(order.getPublisherId())) {
            revieweeId = order.getTakerId();
        } else if (reviewerId.equals(order.getTakerId())) {
            revieweeId = order.getPublisherId();
        } else {
            throw new BizException(ErrorCode.REVIEW_FORBIDDEN);
        }

        Review review = Review.builder()
                .orderId(order.getId())
                .reviewerId(reviewerId)
                .revieweeId(revieweeId)
                .rating(dto.getRating())
                .content(dto.getContent())
                .build();
        review = reviewRepo.save(review);

        // 状态机推进: COMPLETED → REVIEWED
        order.setStatus(OrderStatus.REVIEWED);
        orderRepo.save(order);

        // 通知被评价者
        notifyRepo.save(Notification.builder()
                .userId(revieweeId)
                .title("收到新评价")
                .content("您在课程【" + order.getCourseName() + "】中收到了 " + dto.getRating() + " 星评价。")
                .orderId(order.getId())
                .build());

        log.info("用户[{}]评价订单[{}]，评分={}", reviewerId, order.getId(), dto.getRating());
        return review;
    }

    @Override
    public Page<Review> listByUser(Long userId, int pageNum, int pageSize) {
        return reviewRepo.findByRevieweeId(userId, PageRequest.of(pageNum - 1, pageSize));
    }

    @Override
    public Page<Review> listByOrder(Long orderId, int pageNum, int pageSize) {
        return reviewRepo.findByOrderId(orderId, PageRequest.of(pageNum - 1, pageSize));
    }
}
