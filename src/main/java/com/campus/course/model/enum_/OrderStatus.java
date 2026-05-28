package com.campus.course.model.enum_;

/**
 * 订单状态 —— 状态机核心
 *
 * PUBLISHED → TAKEN → IN_PROGRESS → COMPLETED → REVIEWED
 *    ↓           ↓
 *  CANCELLED  CANCELLED
 */
public enum OrderStatus {

    PUBLISHED("已发布"),       // 代课需求已发布，等待接单
    TAKEN("已接单"),           // 有人接单，等待开始
    IN_PROGRESS("进行中"),     // 代课进行中
    COMPLETED("已完成"),       // 代课完成，待评价
    REVIEWED("已评价"),        // 已完成评价
    CANCELLED("已取消");       // 订单取消

    private final String desc;

    OrderStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
