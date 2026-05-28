package com.campus.course.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名 */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** 密码（实际项目应加密存储） */
    @Column(nullable = false)
    private String password;

    /** 手机号 */
    @Column(length = 20)
    private String phone;

    /** 信用评分 */
    @Column(name = "credit_score")
    @Builder.Default
    private Integer creditScore = 100;

    /** 累计代课次数 */
    @Column(name = "take_count")
    @Builder.Default
    private Integer takeCount = 0;

    /** 累计发布次数 */
    @Column(name = "publish_count")
    @Builder.Default
    private Integer publishCount = 0;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}
