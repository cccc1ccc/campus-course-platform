package com.campus.course.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 消息通知实体
 */
@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 接收者ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 消息标题 */
    @Column(length = 200)
    private String title;

    /** 消息内容 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 是否已读 */
    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    /** 关联订单ID */
    @Column(name = "order_id")
    private Long orderId;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}
