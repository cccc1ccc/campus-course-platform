package com.campus.course.security;

import com.campus.course.common.BizException;
import com.campus.course.common.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具类。
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /** 获取当前登录用户 ID（来自 JWT） */
    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return (Long) auth.getPrincipal();
    }
}
