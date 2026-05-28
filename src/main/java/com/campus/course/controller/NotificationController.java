package com.campus.course.controller;

import com.campus.course.common.Result;
import com.campus.course.model.entity.Notification;
import com.campus.course.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 通知接口
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notifyService;

    /** 获取通知列表 */
    @GetMapping
    public Result<Page<Notification>> list(@RequestHeader("X-User-Id") Long userId,
                                          @RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(notifyService.listByUser(userId, pageNum, pageSize));
    }

    /** 未读数量 */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(notifyService.countUnread(userId));
    }

    /** 标记已读 */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id,
                                 @RequestHeader("X-User-Id") Long userId) {
        notifyService.markRead(id, userId);
        return Result.success();
    }

    /** 全部标记已读 */
    @PutMapping("/read-all")
    public Result<Void> markAllRead(@RequestHeader("X-User-Id") Long userId) {
        notifyService.markAllRead(userId);
        return Result.success();
    }
}
