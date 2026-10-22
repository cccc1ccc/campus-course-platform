-- ═══════════════════════════════════════════════════════════════
--  校园代课对接平台 - 数据库初始化脚本
-- ═══════════════════════════════════════════════════════════════

CREATE DATABASE IF NOT EXISTS campus_course
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE campus_course;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `username`      VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`      VARCHAR(255) NOT NULL COMMENT '密码',
    `phone`         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `credit_score`  INT          NOT NULL DEFAULT 100 COMMENT '信用评分',
    `take_count`    INT          NOT NULL DEFAULT 0   COMMENT '接单次数',
    `publish_count` INT          NOT NULL DEFAULT 0   COMMENT '发布次数',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 代课订单表
CREATE TABLE IF NOT EXISTS `course_order` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT,
    `publisher_id`    BIGINT        NOT NULL COMMENT '发布者ID',
    `taker_id`        BIGINT        DEFAULT NULL COMMENT '接单者ID',
    `course_name`     VARCHAR(100)  NOT NULL COMMENT '课程名称',
    `course_code`     VARCHAR(30)   DEFAULT NULL COMMENT '课程代码',
    `class_time`      VARCHAR(100)  NOT NULL COMMENT '上课时间',
    `class_location`  VARCHAR(100)  DEFAULT NULL COMMENT '上课地点',
    `reward`          DECIMAL(10,2) DEFAULT NULL COMMENT '报酬',
    `description`     TEXT          DEFAULT NULL COMMENT '详细描述',
    `status`          VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED' COMMENT '订单状态',
    `version`         BIGINT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_publisher` (`publisher_id`),
    KEY `idx_taker` (`taker_id`),
    KEY `idx_status` (`status`),
    KEY `idx_course_name` (`course_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代课订单表';

-- 评价表
CREATE TABLE IF NOT EXISTS `review` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `order_id`    BIGINT   NOT NULL COMMENT '订单ID',
    `reviewer_id` BIGINT  NOT NULL COMMENT '评价者ID',
    `reviewee_id` BIGINT  NOT NULL COMMENT '被评价者ID',
    `rating`      INT      NOT NULL COMMENT '评分1~5',
    `content`     TEXT     DEFAULT NULL COMMENT '评价内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order` (`order_id`),
    KEY `idx_reviewee` (`reviewee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 通知表
CREATE TABLE IF NOT EXISTS `notification` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT   NOT NULL COMMENT '接收者ID',
    `title`       VARCHAR(200) DEFAULT NULL COMMENT '标题',
    `content`     TEXT     DEFAULT NULL COMMENT '内容',
    `is_read`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已读',
    `order_id`    BIGINT   DEFAULT NULL COMMENT '关联订单ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ═══════════════════════════════════════════════════════════════
--  示例数据
-- ═══════════════════════════════════════════════════════════════

-- 密码均为 123456（BCrypt 加密存储，示例）
INSERT INTO `user` (`username`, `password`, `phone`) VALUES
('zhangsan', '$2b$10$x/O4svoo6LFiJ3bxk79BkusR76XuMRUo8PFzxH3qF6T.EfsO0pWYq', '13800001111'),
('lisi',     '$2b$10$Mw0o9qjU4fTbanFXYeOGAehQfywQ8..ueEj4OsdAAzv3v3vI/UmFO', '13800002222'),
('wangwu',   '$2b$10$PRIwAf.tW/SmthVFgJMPSus6ZKI.fnEg87rRvhWDbqQCwX/bgbQVa', '13800003333');

INSERT INTO `course_order` (`publisher_id`, `course_name`, `course_code`, `class_time`, `class_location`, `reward`, `description`, `status`) VALUES
(1, '高等数学A', 'MATH1001', '周一 3-4节', '教学楼A301', 50.00, '本周一3-4节高数课需要代课，有笔记报酬', 'PUBLISHED'),
(1, '大学物理实验', 'PHYS2001', '周三 5-6节', '实验楼B205', 80.00, '物理实验课，需穿实验服', 'PUBLISHED'),
(2, '数据结构与算法', 'CS230',   '周五 1-2节', '计算机楼C102', 60.00, 'CS230数据结构课，需要代签到', 'TAKEN');

INSERT INTO `notification` (`user_id`, `title`, `content`, `order_id`) VALUES
(1, '您的代课需求已被接单', '课程【数据结构与算法】已有人接单，请留意上课安排。', 3);
