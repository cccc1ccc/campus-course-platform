package com.campus.course.repository;

import com.campus.course.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByRevieweeId(Long revieweeId, Pageable pageable);

    Page<Review> findByOrderId(Long orderId, Pageable pageable);

    boolean existsByOrderId(Long orderId);
}
