package com.campus.course.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 注册请求
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String phone;
}
