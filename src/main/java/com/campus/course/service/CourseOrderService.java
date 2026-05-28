package com.campus.course.service;

import com.campus.course.dto.CourseOrderCreateDTO;
import com.campus.course.model.entity.CourseOrder;
import com.campus.course.model.enum_.OrderStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface CourseOrderService {

    /** 发布代课需求 */
    CourseOrder publishOrder(Long publisherId, CourseOrderCreateDTO dto);

    /** 接单 —— 状态机: PUBLISHED → TAKEN */
    CourseOrder takeOrder(Long orderId, Long takerId);

    /** 开始代课 —— 状态机: TAKEN → IN_PROGRESS */
    CourseOrder startOrder(Long orderId, Long takerId);

    /** 完成代课 —— 状态机: IN_PROGRESS → COMPLETED */
    CourseOrder completeOrder(Long orderId, Long takerId);

    /** 取消订单 —— 状态机: PUBLISHED/TAKEN → CANCELLED */
    CourseOrder cancelOrder(Long orderId, Long operatorId);

    /** 按状态分页查询 */
    Page<CourseOrder> listByStatus(OrderStatus status, int pageNum, int pageSize);

    /** 搜索课程 */
    Page<CourseOrder> searchCourse(String keyword, int pageNum, int pageSize);

    /** 查询发布者的订单 */
    Page<CourseOrder> listByPublisher(Long publisherId, int pageNum, int pageSize);

    /** 查询接单者的订单 */
    Page<CourseOrder> listByTaker(Long takerId, int pageNum, int pageSize);

    /** 获取热门课程（Redis 缓存） */
    List<Map<String, Object>> getHotCourses(int limit);

    /** 获取订单详情 */
    CourseOrder getOrder(Long orderId);
}
