package com.campus.course.controller;

import com.campus.course.common.Result;
import com.campus.course.model.entity.Notification;
import com.campus.course.security.SecurityUtils;
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
    public Result<Page<Notification>> list(@RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(notifyService.listByUser(SecurityUtils.currentUserId(), pageNum, pageSize));
    }

    /** 未读数量 */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(notifyService.countUnread(SecurityUtils.currentUserId()));
    }

    /** 标记已读 */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notifyService.markRead(id, SecurityUtils.currentUserId());
        return Result.success();
    }

    /** 全部标记已读 */
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        notifyService.markAllRead(SecurityUtils.currentUserId());
        return Result.success();
    }
}
