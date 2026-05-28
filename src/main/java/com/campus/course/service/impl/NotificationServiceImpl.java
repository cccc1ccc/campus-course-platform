package com.campus.course.service.impl;

import com.campus.course.model.entity.Notification;
import com.campus.course.repository.NotificationRepository;
import com.campus.course.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notifyRepo;

    @Override
    public Page<Notification> listByUser(Long userId, int pageNum, int pageSize) {
        return notifyRepo.findByUserIdOrderByCreateTimeDesc(userId, PageRequest.of(pageNum - 1, pageSize));
    }

    @Override
    public long countUnread(Long userId) {
        return notifyRepo.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markRead(Long notifyId, Long userId) {
        Notification n = notifyRepo.findById(notifyId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        if (!n.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此通知");
        }
        n.setIsRead(true);
        notifyRepo.save(n);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        Page<Notification> unread = notifyRepo.findByUserIdOrderByCreateTimeDesc(userId,
                PageRequest.of(0, Integer.MAX_VALUE));
        unread.getContent().stream()
                .filter(n -> !n.getIsRead())
                .forEach(n -> n.setIsRead(true));
        notifyRepo.saveAll(unread.getContent());
    }
}
