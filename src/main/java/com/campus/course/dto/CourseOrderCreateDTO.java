package com.campus.course.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 发布代课需求 DTO
 */
@Data
public class CourseOrderCreateDTO {

    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    private String courseCode;

    @NotBlank(message = "上课时间不能为空")
    private String classTime;

    private String classLocation;

    @NotNull(message = "报酬不能为空")
    private BigDecimal reward;

    private String description;
}
