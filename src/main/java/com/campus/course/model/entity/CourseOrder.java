package com.campus.course.model.entity;

import com.campus.course.model.enum_.OrderStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 代课订单实体
 */
@Entity
@Table(name = "course_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 发布者（需要代课的人） */
    @Column(name = "publisher_id", nullable = false)
    private Long publisherId;

    /** 接单者（代课的人） */
    @Column(name = "taker_id")
    private Long takerId;

    /** 课程名称 */
    @Column(nullable = false, length = 100)
    private String courseName;

    /** 课程代码 */
    @Column(name = "course_code", length = 30)
    private String courseCode;

    /** 上课时间描述 */
    @Column(name = "class_time", nullable = false, length = 100)
    private String classTime;

    /** 上课地点 */
    @Column(name = "class_location", length = 100)
    private String classLocation;

    /** 代课报酬 */
    @Column(precision = 10, scale = 2)
    private BigDecimal reward;

    /** 详细描述 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 订单状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    /** 乐观锁版本号（防止并发接单） */
    @Version
    @Column(nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
