package com.campus.course.common;

import lombok.Getter;

/**
 * 统一业务错误码。
 */
@Getter
public enum ErrorCode {

    // 通用
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限执行此操作"),

    // 用户
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    USERNAME_OR_PASSWORD_ERROR(1003, "用户名或密码错误"),

    // 订单
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_STATUS_ERROR(2002, "订单状态异常"),
    CANNOT_TAKE_OWN_ORDER(2003, "不能接自己发布的代课需求"),
    ORDER_ALREADY_TAKEN(2004, "订单已被他人接单"),
    ORDER_NOT_CANCELLABLE(2005, "当前状态不可取消"),
    ORDER_NOT_OPERABLE(2006, "无权操作此订单"),

    // 评价
    REVIEW_NOT_ALLOWED(3001, "当前状态无法评价"),
    REVIEW_DUPLICATED(3002, "该订单已评价，不能重复评价"),
    REVIEW_FORBIDDEN(3003, "无权评价此订单"),

    // 通知
    NOTIFICATION_NOT_FOUND(4001, "通知不存在"),
    NOTIFICATION_NOT_OWNED(4002, "无权操作此通知"),

    // 系统
    SERVER_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
