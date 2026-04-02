package com.rnclasses.repository;

import com.rnclasses.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find by order ID (for verification)
    Optional<Payment> findByOrderId(String orderId);
    
    // Find by payment ID
    Optional<Payment> findByPaymentId(String paymentId);
    
    // Find all payments by user
    List<Payment> findByUserId(Long userId);
    
    // Find all payments by course
    List<Payment> findByCourseId(Long courseId);
    
    // Find payments by status
    List<Payment> findByStatus(String status);
    
    // Check if payment exists for order
    boolean existsByOrderId(String orderId);
    
    // Find payments within date range
    List<Payment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);
    
    // Get total revenue
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCESS'")
    Long getTotalRevenue();
    
    // Get revenue by date range
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCESS' AND p.paymentDate BETWEEN :start AND :end")
    Long getRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Count payments by status
    @Query("SELECT p.status, COUNT(p) FROM Payment p GROUP BY p.status")
    List<Object[]> countPaymentsByStatus();
}