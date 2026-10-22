package com.campus.course.service;

import com.campus.course.common.BizException;
import com.campus.course.common.ErrorCode;
import com.campus.course.dto.LoginDTO;
import com.campus.course.dto.RegisterDTO;
import com.campus.course.model.entity.User;
import com.campus.course.repository.UserRepository;
import com.campus.course.security.JwtUtil;
import com.campus.course.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepo;
    @Mock private JwtUtil jwtUtil;

    private AuthServiceImpl service;
    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(userRepo, encoder, jwtUtil);
    }

    @Test
    void register_success_passwordEncrypted() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");

        when(userRepo.existsByUsername("newuser")).thenReturn(false);
        when(userRepo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtUtil.generateToken(any(), any())).thenReturn("token");

        var resp = service.register(dto);

        assertNotNull(resp.getToken());
        // 验证密码被加密（不是明文），且保存时已加密
        verify(userRepo).save(argThat(u -> !"123456".equals(u.getPassword())
                && encoder.matches("123456", u.getPassword())));
    }

    @Test
    void register_duplicateUsername_throws() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("exists");
        dto.setPassword("123456");

        when(userRepo.existsByUsername("exists")).thenReturn(true);

        BizException e = assertThrows(BizException.class, () -> service.register(dto));
        assertEquals(ErrorCode.USERNAME_EXISTS.getCode(), e.getCode());
    }

    @Test
    void login_success() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("zhangsan");
        dto.setPassword("123456");

        User user = User.builder()
                .id(1L)
                .username("zhangsan")
                .password(encoder.encode("123456"))
                .build();

        when(userRepo.findByUsername("zhangsan")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(), any())).thenReturn("token");

        var resp = service.login(dto);
        assertEquals("token", resp.getToken());
    }

    @Test
    void login_wrongPassword_throws() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("zhangsan");
        dto.setPassword("wrong");

        User user = User.builder()
                .id(1L)
                .username("zhangsan")
                .password(encoder.encode("123456"))
                .build();

        when(userRepo.findByUsername("zhangsan")).thenReturn(Optional.of(user));

        BizException e = assertThrows(BizException.class, () -> service.login(dto));
        assertEquals(ErrorCode.USERNAME_OR_PASSWORD_ERROR.getCode(), e.getCode());
    }
}
