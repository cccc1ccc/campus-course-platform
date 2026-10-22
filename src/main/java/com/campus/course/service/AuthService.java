package com.campus.course.service;

import com.campus.course.dto.AuthResponse;
import com.campus.course.dto.LoginDTO;
import com.campus.course.dto.RegisterDTO;

/**
 * 认证服务：注册与登录。
 */
public interface AuthService {

    AuthResponse register(RegisterDTO dto);

    AuthResponse login(LoginDTO dto);
}
