package com.campus.course.service;

import com.campus.course.dto.ReviewCreateDTO;
import com.campus.course.model.entity.Review;
import org.springframework.data.domain.Page;

/**
 * 评价服务接口
 */
public interface ReviewService {

    /** 提交评价 —— 订单状态机: COMPLETED → REVIEWED */
    Review createReview(Long reviewerId, ReviewCreateDTO dto);

    /** 查看某用户的评价 */
    Page<Review> listByUser(Long userId, int pageNum, int pageSize);

    /** 查看某订单的评价 */
    Page<Review> listByOrder(Long orderId, int pageNum, int pageSize);
}
