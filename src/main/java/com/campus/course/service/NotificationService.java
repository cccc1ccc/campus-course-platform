package com.campus.course.service;

import com.campus.course.model.entity.Notification;
import org.springframework.data.domain.Page;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /** 获取用户通知列表 */
    Page<Notification> listByUser(Long userId, int pageNum, int pageSize);

    /** 获取未读数量 */
    long countUnread(Long userId);

    /** 标记已读 */
    void markRead(Long notifyId, Long userId);

    /** 全部标记已读 */
    void markAllRead(Long userId);
}
