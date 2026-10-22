package com.campus.course.service;

import com.campus.course.common.BizException;
import com.campus.course.common.ErrorCode;
import com.campus.course.dto.ReviewCreateDTO;
import com.campus.course.model.entity.CourseOrder;
import com.campus.course.model.entity.Review;
import com.campus.course.model.enum_.OrderStatus;
import com.campus.course.repository.CourseOrderRepository;
import com.campus.course.repository.NotificationRepository;
import com.campus.course.repository.ReviewRepository;
import com.campus.course.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepo;
    @Mock private CourseOrderRepository orderRepo;
    @Mock private NotificationRepository notifyRepo;

    private ReviewServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReviewServiceImpl(reviewRepo, orderRepo, notifyRepo);
    }

    private CourseOrder completedOrder(Long publisherId, Long takerId) {
        return CourseOrder.builder()
                .id(1L)
                .publisherId(publisherId)
                .takerId(takerId)
                .courseName("高等数学A")
                .status(OrderStatus.COMPLETED)
                .build();
    }

    private ReviewCreateDTO dto(int rating) {
        ReviewCreateDTO dto = new ReviewCreateDTO();
        dto.setOrderId(1L);
        dto.setRating(rating);
        dto.setContent("好评");
        return dto;
    }

    @Test
    void createReview_success() {
        CourseOrder order = completedOrder(1L, 2L);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(reviewRepo.existsByOrderId(1L)).thenReturn(false);
        when(reviewRepo.save(any(Review.class))).thenAnswer(i -> i.getArgument(0));

        Review review = service.createReview(1L, dto(5));

        assertEquals(5, review.getRating());
        assertEquals(2L, review.getRevieweeId());
        assertEquals(OrderStatus.REVIEWED, order.getStatus());
        verify(notifyRepo).save(any());
    }

    @Test
    void createReview_notCompleted_throws() {
        CourseOrder order = completedOrder(1L, 2L);
        order.setStatus(OrderStatus.IN_PROGRESS);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        BizException e = assertThrows(BizException.class, () -> service.createReview(1L, dto(5)));
        assertEquals(ErrorCode.REVIEW_NOT_ALLOWED.getCode(), e.getCode());
    }

    @Test
    void createReview_duplicated_throws() {
        CourseOrder order = completedOrder(1L, 2L);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(reviewRepo.existsByOrderId(1L)).thenReturn(true);

        BizException e = assertThrows(BizException.class, () -> service.createReview(1L, dto(5)));
        assertEquals(ErrorCode.REVIEW_DUPLICATED.getCode(), e.getCode());
    }

    @Test
    void createReview_forbidden_throws() {
        CourseOrder order = completedOrder(1L, 2L);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(reviewRepo.existsByOrderId(1L)).thenReturn(false);

        // 第三方用户 3 既非发布者也非接单者
        BizException e = assertThrows(BizException.class, () -> service.createReview(3L, dto(5)));
        assertEquals(ErrorCode.REVIEW_FORBIDDEN.getCode(), e.getCode());
    }
}
