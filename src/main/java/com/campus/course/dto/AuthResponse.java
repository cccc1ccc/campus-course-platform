package com.campus.course.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录/注册成功返回：token + 用户信息。
 */
@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private Long userId;
    private String username;
}
