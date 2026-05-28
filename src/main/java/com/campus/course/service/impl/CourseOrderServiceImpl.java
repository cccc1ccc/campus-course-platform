package com.campus.course.service.impl;

import com.campus.course.dto.CourseOrderCreateDTO;
import com.campus.course.model.entity.CourseOrder;
import com.campus.course.model.entity.Notification;
import com.campus.course.model.entity.User;
import com.campus.course.model.enum_.OrderStatus;
import com.campus.course.repository.CourseOrderRepository;
import com.campus.course.repository.NotificationRepository;
import com.campus.course.repository.UserRepository;
import com.campus.course.service.CourseOrderService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseOrderServiceImpl implements CourseOrderService {

    private final CourseOrderRepository orderRepo;
    private final UserRepository userRepo;
    private final NotificationRepository notifyRepo;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String HOT_COURSES_KEY = "campus:hot_courses";
    private static final long HOT_COURSES_TTL_HOURS = 2;

    // ═══════════════════════════════════════════════════════════
    //  状态机转换核心
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public CourseOrder publishOrder(Long publisherId, CourseOrderCreateDTO dto) {
        User publisher = userRepo.findById(publisherId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        CourseOrder order = CourseOrder.builder()
                .publisherId(publisherId)
                .courseName(dto.getCourseName())
                .courseCode(dto.getCourseCode())
                .classTime(dto.getClassTime())
                .classLocation(dto.getClassLocation())
                .reward(dto.getReward())
                .description(dto.getDescription())
                .status(OrderStatus.PUBLISHED)
                .build();

        order = orderRepo.save(order);

        // 更新发布次数
        publisher.setPublishCount(publisher.getPublishCount() + 1);
        userRepo.save(publisher);

        // 使热门课程缓存失效
        redisTemplate.delete(HOT_COURSES_KEY);

        log.info("用户[{}]发布了代课需求[{}]", publisherId, order.getId());
        return order;
    }

    @Override
    @Transactional
    public CourseOrder takeOrder(Long orderId, Long takerId) {
        CourseOrder order = getOrderAndValidate(orderId, OrderStatus.PUBLISHED);

        // 不能接自己发布的单
        if (order.getPublisherId().equals(takerId)) {
            throw new RuntimeException("不能接自己发布的代课需求");
        }

        order.setTakerId(takerId);
        order.setStatus(OrderStatus.TAKEN);
        order = orderRepo.save(order);

        // 更新接单次数
        User taker = userRepo.findById(takerId)
                .orElseThrow(() -> new RuntimeException("接单用户不存在"));
        taker.setTakeCount(taker.getTakeCount() + 1);
        userRepo.save(taker);

        // 通知发布者
        sendNotification(order.getPublisherId(), "您的代课需求已被接单",
                "课程【" + order.getCourseName() + "】已有人接单，请留意上课安排。", orderId);

        log.info("用户[{}]接单[{}]", takerId, orderId);
        return order;
    }

    @Override
    @Transactional
    public CourseOrder startOrder(Long orderId, Long takerId) {
        CourseOrder order = getOrderAndValidate(orderId, OrderStatus.TAKEN);
        validateTaker(order, takerId);

        order.setStatus(OrderStatus.IN_PROGRESS);
        order = orderRepo.save(order);

        sendNotification(order.getPublisherId(), "代课已开始",
                "课程【" + order.getCourseName() + "】的代课已开始。", orderId);

        return order;
    }

    @Override
    @Transactional
    public CourseOrder completeOrder(Long orderId, Long takerId) {
        CourseOrder order = getOrderAndValidate(orderId, OrderStatus.IN_PROGRESS);
        validateTaker(order, takerId);

        order.setStatus(OrderStatus.COMPLETED);
        order = orderRepo.save(order);

        sendNotification(order.getPublisherId(), "代课已完成",
                "课程【" + order.getCourseName() + "】的代课已完成，请评价。", orderId);

        return order;
    }

    @Override
    @Transactional
    public CourseOrder cancelOrder(Long orderId, Long operatorId) {
        CourseOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (order.getStatus() != OrderStatus.PUBLISHED && order.getStatus() != OrderStatus.TAKEN) {
            throw new RuntimeException("当前状态【" + order.getStatus().getDesc() + "】不可取消");
        }

        // 只有发布者或接单者可取消
        if (!order.getPublisherId().equals(operatorId) &&
            (order.getTakerId() == null || !order.getTakerId().equals(operatorId))) {
            throw new RuntimeException("无权取消此订单");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepo.save(order);

        // 通知对方
        Long notifyUserId = operatorId.equals(order.getPublisherId()) ? order.getTakerId() : order.getPublisherId();
        if (notifyUserId != null) {
            sendNotification(notifyUserId, "订单已取消",
                    "课程【" + order.getCourseName() + "】的代课订单已取消。", orderId);
        }

        return order;
    }

    // ═══════════════════════════════════════════════════════════
    //  查询功能
    // ═══════════════════════════════════════════════════════════

    @Override
    public Page<CourseOrder> listByStatus(OrderStatus status, int pageNum, int pageSize) {
        return orderRepo.findByStatus(status, PageRequest.of(pageNum - 1, pageSize));
    }

    @Override
    public Page<CourseOrder> searchCourse(String keyword, int pageNum, int pageSize) {
        return orderRepo.findByCourseNameContainingAndStatus(
                keyword, OrderStatus.PUBLISHED, PageRequest.of(pageNum - 1, pageSize));
    }

    @Override
    public Page<CourseOrder> listByPublisher(Long publisherId, int pageNum, int pageSize) {
        return orderRepo.findByPublisherId(publisherId, PageRequest.of(pageNum - 1, pageSize));
    }

    @Override
    public Page<CourseOrder> listByTaker(Long takerId, int pageNum, int pageSize) {
        return orderRepo.findByTakerId(takerId, PageRequest.of(pageNum - 1, pageSize));
    }

    @Override
    public CourseOrder getOrder(Long orderId) {
        return orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    // ═══════════════════════════════════════════════════════════
    //  热门课程（Redis 缓存）
    // ═══════════════════════════════════════════════════════════

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getHotCourses(int limit) {
        // 1. 查 Redis 缓存
        String cached = redisTemplate.opsForValue().get(HOT_COURSES_KEY);
        if (cached != null) {
            try {
                List<Map<String, Object>> result = objectMapper.readValue(cached,
                        new TypeReference<List<Map<String, Object>>>() {});
                log.debug("热门课程命中 Redis 缓存");
                return result.stream().limit(limit).collect(Collectors.toList());
            } catch (Exception e) {
                log.warn("Redis 缓存反序列化失败，回源数据库", e);
            }
        }

        // 2. 查数据库
        List<Object[]> rows = orderRepo.findHotCourses(PageRequest.of(0, limit));
        List<Map<String, Object>> hotCourses = rows.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("courseName", row[0]);
            map.put("count", row[1]);
            return map;
        }).collect(Collectors.toList());

        // 3. 写入 Redis
        try {
            String json = objectMapper.writeValueAsString(hotCourses);
            redisTemplate.opsForValue().set(HOT_COURSES_KEY, json, HOT_COURSES_TTL_HOURS, TimeUnit.HOURS);
            log.debug("热门课程已写入 Redis，TTL={}h", HOT_COURSES_TTL_HOURS);
        } catch (Exception e) {
            log.warn("Redis 缓存写入失败", e);
        }

        return hotCourses;
    }

    // ═══════════════════════════════════════════════════════════
    //  内部辅助
    // ═══════════════════════════════════════════════════════════

    /** 获取订单并验证当前状态 */
    private CourseOrder getOrderAndValidate(Long orderId, OrderStatus expected) {
        CourseOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        if (order.getStatus() != expected) {
            throw new RuntimeException("订单状态异常：当前为【" + order.getStatus().getDesc() +
                    "】，期望【" + expected.getDesc() + "】");
        }
        return order;
    }

    /** 验证操作者是接单人 */
    private void validateTaker(CourseOrder order, Long takerId) {
        if (!takerId.equals(order.getTakerId())) {
            throw new RuntimeException("无权操作此订单");
        }
    }

    /** 发送站内通知 */
    private void sendNotification(Long userId, String title, String content, Long orderId) {
        Notification notify = Notification.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .orderId(orderId)
                .build();
        notifyRepo.save(notify);
    }
}
