package com.campus.course.controller;

import com.campus.course.common.Result;
import com.campus.course.dto.CourseOrderCreateDTO;
import com.campus.course.model.entity.CourseOrder;
import com.campus.course.model.enum_.OrderStatus;
import com.campus.course.service.CourseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 代课订单接口
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class CourseOrderController {

    private final CourseOrderService orderService;

    /** 发布代课需求 */
    @PostMapping
    public Result<CourseOrder> publish(@RequestHeader("X-User-Id") Long userId,
                                      @Validated @RequestBody CourseOrderCreateDTO dto) {
        return Result.success(orderService.publishOrder(userId, dto));
    }

    /** 接单 */
    @PutMapping("/{id}/take")
    public Result<CourseOrder> take(@PathVariable Long id,
                                    @RequestHeader("X-User-Id") Long userId) {
        return Result.success(orderService.takeOrder(id, userId));
    }

    /** 开始代课 */
    @PutMapping("/{id}/start")
    public Result<CourseOrder> start(@PathVariable Long id,
                                     @RequestHeader("X-User-Id") Long userId) {
        return Result.success(orderService.startOrder(id, userId));
    }

    /** 完成代课 */
    @PutMapping("/{id}/complete")
    public Result<CourseOrder> complete(@PathVariable Long id,
                                        @RequestHeader("X-User-Id") Long userId) {
        return Result.success(orderService.completeOrder(id, userId));
    }

    /** 取消订单 */
    @PutMapping("/{id}/cancel")
    public Result<CourseOrder> cancel(@PathVariable Long id,
                                      @RequestHeader("X-User-Id") Long userId) {
        return Result.success(orderService.cancelOrder(id, userId));
    }

    /** 按状态查询订单列表 */
    @GetMapping
    public Result<Page<CourseOrder>> listByStatus(
            @RequestParam(defaultValue = "PUBLISHED") OrderStatus status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(orderService.listByStatus(status, pageNum, pageSize));
    }

    /** 搜索课程 */
    @GetMapping("/search")
    public Result<Page<CourseOrder>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(orderService.searchCourse(keyword, pageNum, pageSize));
    }

    /** 我发布的订单 */
    @GetMapping("/published")
    public Result<Page<CourseOrder>> myPublished(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(orderService.listByPublisher(userId, pageNum, pageSize));
    }

    /** 我接的订单 */
    @GetMapping("/taken")
    public Result<Page<CourseOrder>> myTaken(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(orderService.listByTaker(userId, pageNum, pageSize));
    }

    /** 订单详情 */
    @GetMapping("/{id}")
    public Result<CourseOrder> detail(@PathVariable Long id) {
        return Result.success(orderService.getOrder(id));
    }

    /** 热门课程 */
    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> hotCourses(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(orderService.getHotCourses(limit));
    }
}
