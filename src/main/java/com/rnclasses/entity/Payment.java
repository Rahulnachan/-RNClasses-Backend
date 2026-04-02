package com.rnclasses.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "payment_id", nullable = false, unique = true)
    private String paymentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public Payment() {
        this.createdAt = LocalDateTime.now();
    }

    // Parameterized constructor for easy creation
    public Payment(String orderId, String paymentId, Long userId, Long courseId, 
                   Integer amount, String couponCode, String status) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.userId = userId;
        this.courseId = courseId;
        this.amount = amount;
        this.couponCode = couponCode;
        this.status = status;
        this.paymentDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    // ========== EXISTING GETTERS AND SETTERS ==========
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ========== HELPER METHODS FOR SERVICES ==========

    /**
     * Check if payment was successful
     */
    public boolean isSuccessful() {
        return "SUCCESS".equalsIgnoreCase(this.status);
    }

    /**
     * Check if payment failed
     */
    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(this.status);
    }

    /**
     * Mark payment as successful
     */
    public void markAsSuccessful() {
        this.status = "SUCCESS";
        this.paymentDate = LocalDateTime.now();
    }

    /**
     * Mark payment as failed
     */
    public void markAsFailed() {
        this.status = "FAILED";
    }

    /**
     * Get formatted payment date
     */
    public String getFormattedPaymentDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        return this.paymentDate != null ? this.paymentDate.format(formatter) : "";
    }

    /**
     * Get payment amount in rupees (with currency symbol)
     */
    public String getFormattedAmount() {
        return "₹" + (this.amount != null ? this.amount : 0);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", orderId='" + orderId + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", userId=" + userId +
                ", courseId=" + courseId +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", paymentDate=" + paymentDate +
                '}';
    }
}