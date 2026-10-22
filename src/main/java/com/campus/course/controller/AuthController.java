package com.campus.course.controller;

import com.campus.course.common.Result;
import com.campus.course.dto.AuthResponse;
import com.campus.course.dto.LoginDTO;
import com.campus.course.dto.RegisterDTO;
import com.campus.course.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：注册与登录。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 注册 */
    @PostMapping("/register")
    public Result<AuthResponse> register(@Validated @RequestBody RegisterDTO dto) {
        return Result.success(authService.register(dto));
    }

    /** 登录 */
    @PostMapping("/login")
    public Result<AuthResponse> login(@Validated @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }
}
