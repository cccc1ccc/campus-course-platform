package com.campus.course.service.impl;

import com.campus.course.common.BizException;
import com.campus.course.common.ErrorCode;
import com.campus.course.dto.AuthResponse;
import com.campus.course.dto.LoginDTO;
import com.campus.course.dto.RegisterDTO;
import com.campus.course.model.entity.User;
import com.campus.course.repository.UserRepository;
import com.campus.course.security.JwtUtil;
import com.campus.course.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterDTO dto) {
        if (userRepo.existsByUsername(dto.getUsername())) {
            throw new BizException(ErrorCode.USERNAME_EXISTS);
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .build();
        user = userRepo.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("新用户注册: {}", user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }

    @Override
    public AuthResponse login(LoginDTO dto) {
        User user = userRepo.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BizException(ErrorCode.USERNAME_OR_PASSWORD_ERROR));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.USERNAME_OR_PASSWORD_ERROR);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }
}
