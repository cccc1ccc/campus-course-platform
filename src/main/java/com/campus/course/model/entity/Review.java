package com.campus.course.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 评价实体
 */
@Entity
@Table(name = "review")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联的订单ID */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /** 评价者ID */
    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    /** 被评价者ID */
    @Column(name = "reviewee_id", nullable = false)
    private Long revieweeId;

    /** 评分 1~5 */
    @Column(nullable = false)
    private Integer rating;

    /** 评价内容 */
    @Column(columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}
