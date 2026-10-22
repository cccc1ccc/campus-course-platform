package com.campus.course.repository;

import com.campus.course.model.entity.CourseOrder;
import com.campus.course.model.enum_.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseOrderRepository extends JpaRepository<CourseOrder, Long> {

    /** 按状态分页查询 */
    Page<CourseOrder> findByStatus(OrderStatus status, Pageable pageable);

    /** 按发布者查询 */
    Page<CourseOrder> findByPublisherId(Long publisherId, Pageable pageable);

    /** 按接单者查询 */
    Page<CourseOrder> findByTakerId(Long takerId, Pageable pageable);

    /** 模糊搜索课程名称 */
    Page<CourseOrder> findByCourseNameContainingAndStatus(String keyword, OrderStatus status, Pageable pageable);

    /** 查询热门课程（按发布频次排序） */
    @Query("SELECT o.courseName, COUNT(o) AS cnt FROM CourseOrder o " +
           "GROUP BY o.courseName ORDER BY cnt DESC")
    List<Object[]> findHotCourses(Pageable pageable);

    /** 查询发布者某状态下的订单数（防止重复接单） */
    boolean existsByPublisherIdAndStatus(Long publisherId, OrderStatus status);

    /**
     * 原子抢单：仅当订单仍为 PUBLISHED 时才能被接单，返回受影响行数。
     * 配合 @Version 乐观锁，确保并发场景下只有一个请求成功。
     */
    @Modifying
    @Query("UPDATE CourseOrder o SET o.takerId = :takerId, o.status = :taken " +
           "WHERE o.id = :orderId AND o.status = :published")
    int takeOrderAtomically(@Param("orderId") Long orderId,
                            @Param("takerId") Long takerId,
                            @Param("published") OrderStatus published,
                            @Param("taken") OrderStatus taken);
}
