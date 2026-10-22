package com.campus.course.service;

import com.campus.course.common.BizException;
import com.campus.course.common.ErrorCode;
import com.campus.course.dto.CourseOrderCreateDTO;
import com.campus.course.model.entity.CourseOrder;
import com.campus.course.model.entity.User;
import com.campus.course.model.enum_.OrderStatus;
import com.campus.course.repository.CourseOrderRepository;
import com.campus.course.repository.NotificationRepository;
import com.campus.course.repository.UserRepository;
import com.campus.course.service.impl.CourseOrderServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseOrderServiceImplTest {

    @Mock private CourseOrderRepository orderRepo;
    @Mock private UserRepository userRepo;
    @Mock private NotificationRepository notifyRepo;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private ObjectMapper objectMapper;

    private CourseOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CourseOrderServiceImpl(orderRepo, userRepo, notifyRepo, redisTemplate, objectMapper);
    }

    private CourseOrder order(Long id, Long publisherId, OrderStatus status) {
        return CourseOrder.builder()
                .id(id)
                .publisherId(publisherId)
                .courseName("高等数学A")
                .classTime("周一 3-4节")
                .status(status)
                .build();
    }

    private CourseOrderCreateDTO dto() {
        CourseOrderCreateDTO dto = new CourseOrderCreateDTO();
        dto.setCourseName("高等数学A");
        dto.setClassTime("周一 3-4节");
        dto.setReward(new BigDecimal("50.00"));
        return dto;
    }

    @Test
    void publishOrder_success() {
        User publisher = User.builder().id(1L).publishCount(0).build();
        when(userRepo.findById(1L)).thenReturn(Optional.of(publisher));
        when(orderRepo.save(any(CourseOrder.class))).thenAnswer(i -> i.getArgument(0));

        CourseOrder result = service.publishOrder(1L, dto());

        assertNotNull(result);
        assertEquals(OrderStatus.PUBLISHED, result.getStatus());
        assertEquals(1, publisher.getPublishCount());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void publishOrder_userNotFound_throws() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        BizException e = assertThrows(BizException.class, () -> service.publishOrder(99L, dto()));
        assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), e.getCode());
    }

    @Test
    void takeOrder_cannotTakeOwnOrder() {
        CourseOrder order = order(1L, 1L, OrderStatus.PUBLISHED);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        BizException e = assertThrows(BizException.class, () -> service.takeOrder(1L, 1L));
        assertEquals(ErrorCode.CANNOT_TAKE_OWN_ORDER.getCode(), e.getCode());
    }

    @Test
    void takeOrder_alreadyTaken_throws() {
        CourseOrder order = order(1L, 1L, OrderStatus.PUBLISHED);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepo.takeOrderAtomically(eq(1L), eq(2L), eq(OrderStatus.PUBLISHED), eq(OrderStatus.TAKEN)))
                .thenReturn(0);

        BizException e = assertThrows(BizException.class, () -> service.takeOrder(1L, 2L));
        assertEquals(ErrorCode.ORDER_ALREADY_TAKEN.getCode(), e.getCode());
    }

    @Test
    void takeOrder_success() {
        CourseOrder order = order(1L, 1L, OrderStatus.PUBLISHED);
        User taker = User.builder().id(2L).takeCount(0).build();
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepo.takeOrderAtomically(eq(1L), eq(2L), eq(OrderStatus.PUBLISHED), eq(OrderStatus.TAKEN)))
                .thenReturn(1);
        when(userRepo.findById(2L)).thenReturn(Optional.of(taker));

        CourseOrder result = service.takeOrder(1L, 2L);

        assertEquals(OrderStatus.TAKEN, result.getStatus());
        assertEquals(2L, result.getTakerId());
        assertEquals(1, taker.getTakeCount());
        verify(notifyRepo).save(any());
    }

    @Test
    void startOrder_wrongTaker_throws() {
        CourseOrder order = order(1L, 1L, OrderStatus.TAKEN);
        order.setTakerId(2L);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        BizException e = assertThrows(BizException.class, () -> service.startOrder(1L, 3L));
        assertEquals(ErrorCode.ORDER_NOT_OPERABLE.getCode(), e.getCode());
    }

    @Test
    void cancelOrder_wrongStatus_throws() {
        CourseOrder order = order(1L, 1L, OrderStatus.COMPLETED);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        BizException e = assertThrows(BizException.class, () -> service.cancelOrder(1L, 1L));
        assertEquals(ErrorCode.ORDER_NOT_CANCELLABLE.getCode(), e.getCode());
    }
}
