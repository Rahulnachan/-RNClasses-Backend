package com.rnclasses.repository;

import com.rnclasses.entity.Copun;  // CHANGED: from Coupon to Copun
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Copun, Long> {  // CHANGED: from Coupon to Copun
    
    // Find coupon by code
    Optional<Copun> findByCode(String code);  // CHANGED: from Coupon to Copun
    
    // Find active coupon by code
    @Query("SELECT c FROM Copun c WHERE c.code = :code AND c.isActive = true AND c.validFrom <= CURRENT_TIMESTAMP AND c.validUntil >= CURRENT_TIMESTAMP")
    Optional<Copun> findActiveCouponByCode(@Param("code") String code);  // CHANGED: from Coupon to Copun
    
    // Find all active coupons
    @Query("SELECT c FROM Copun c WHERE c.isActive = true AND c.validFrom <= CURRENT_TIMESTAMP AND c.validUntil >= CURRENT_TIMESTAMP")
    List<Copun> findAllActiveCoupons();  // CHANGED: from Coupon to Copun
    
    // Find expired coupons
    @Query("SELECT c FROM Copun c WHERE c.validUntil < CURRENT_TIMESTAMP")
    List<Copun> findExpiredCoupons();  // CHANGED: from Coupon to Copun
    
    // Find coupons by discount percentage range
    List<Copun> findByDiscountPercentageBetween(Integer min, Integer max);  // CHANGED: from Coupon to Copun
    
    // Check if code exists
    boolean existsByCode(String code);
}