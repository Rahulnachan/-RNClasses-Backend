package com.rnclasses.service;

import com.rnclasses.entity.Payment;
import com.rnclasses.entity.Enrollment;
import com.rnclasses.entity.Course;
import com.rnclasses.entity.User;
import com.rnclasses.repository.PaymentRepository;
import com.rnclasses.repository.EnrollmentRepository;
import com.rnclasses.repository.CourseRepository;
import com.rnclasses.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Value("${razorpay.key.secret:rzp_test_8t7gHkL9mN4pQ2rS}")
    private String razorpaySecret;

    /**
     * Verify Razorpay payment signature
     */
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(razorpaySecret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes());
            String expectedSignature = Base64.getEncoder().encodeToString(hash);
            return expectedSignature.equals(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Save payment details after successful payment
     */
    @Transactional
    public Payment savePayment(String orderId, String paymentId, Long courseId, String couponCode, Integer amount) {
        // Check if payment already exists
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(orderId);
        if (existingPayment.isPresent()) {
            throw new RuntimeException("Payment already processed for this order");
        }
        
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentId(paymentId);
        payment.setCourseId(courseId);
        payment.setUserId(getCurrentUserId());
        payment.setAmount(amount != null ? amount : 0);
        payment.setCouponCode(couponCode);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("SUCCESS");
        
        return paymentRepository.save(payment);
    }

    /**
     * Enroll user in course after successful payment
     */
    @Transactional
    public Enrollment enrollUserInCourse(Long courseId) {
        Long userId = getCurrentUserId();
        
        // Fetch the actual User and Course entities
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        
        // Check if already enrolled using the repository method
        if (enrollmentRepository.existsByUserAndCourse(user, course)) {
            throw new RuntimeException("Already enrolled in this course");
        }
        
        // Create enrollment using the entity constructor
        Enrollment enrollment = new Enrollment(user, course);
        
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        
        // Update course student count
        if (course.getStudentsCount() == null) {
            course.setStudentsCount(1);
        } else {
            course.setStudentsCount(course.getStudentsCount() + 1);
        }
        courseRepository.save(course);
        
        return savedEnrollment;
    }

    /**
     * Process complete payment and enrollment flow
     */
    @Transactional
    public Map<String, Object> processPaymentAndEnrollment(String orderId, String paymentId, 
                                                           Long courseId, String couponCode, 
                                                           Integer amount, String signature) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. Verify signature
            boolean isValid = verifySignature(orderId, paymentId, signature);
            if (!isValid) {
                response.put("success", false);
                response.put("message", "Invalid payment signature");
                return response;
            }
            
            // 2. Save payment
            Payment payment = savePayment(orderId, paymentId, courseId, couponCode, amount);
            
            // 3. Enroll user in course
            Enrollment enrollment = enrollUserInCourse(courseId);
            
            // 4. Prepare success response
            Map<String, Object> paymentMap = new HashMap<>();
            paymentMap.put("id", payment.getId());
            paymentMap.put("orderId", payment.getOrderId());
            paymentMap.put("paymentId", payment.getPaymentId());
            paymentMap.put("amount", payment.getAmount());
            
            Map<String, Object> enrollmentMap = new HashMap<>();
            enrollmentMap.put("id", enrollment.getId());
            enrollmentMap.put("status", enrollment.getStatus());
            enrollmentMap.put("enrolledAt", enrollment.getEnrolledAt());
            
            response.put("success", true);
            response.put("message", "Payment successful and enrollment completed");
            response.put("payment", paymentMap);
            response.put("enrollment", enrollmentMap);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }

    /**
     * Get user's payment history
     */
    public List<Payment> getUserPayments() {
        Long userId = getCurrentUserId();
        return paymentRepository.findByUserId(userId);
    }

    /**
     * Get payment by ID
     */
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    /**
     * Get payment by order ID
     */
    public Optional<Payment> getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    /**
     * Apply coupon code
     */
    public Map<String, Object> applyCoupon(String code, int amount) {
        Map<String, Object> response = new HashMap<>();
        
        // In a real application, you would fetch this from database
        Map<String, Integer> validCoupons = new HashMap<>();
        validCoupons.put("RNCLASSES10", 10);
        validCoupons.put("FESTIVE20", 20);
        validCoupons.put("DIWALI25", 25);
        validCoupons.put("NEWYEAR30", 30);
        
        if (validCoupons.containsKey(code)) {
            int discountPercentage = validCoupons.get(code);
            int discountAmount = (amount * discountPercentage) / 100;
            int finalAmount = amount - discountAmount;
            
            Map<String, Object> couponMap = new HashMap<>();
            couponMap.put("code", code);
            couponMap.put("discount", discountPercentage);
            
            response.put("success", true);
            response.put("discountPercentage", discountPercentage);
            response.put("discountAmount", discountAmount);
            response.put("finalAmount", finalAmount);
            response.put("coupon", couponMap);
        } else {
            response.put("success", false);
            response.put("message", "Invalid coupon code");
        }
        
        return response;
    }

    /**
     * Get current authenticated user ID from SecurityContext
     * FIXED: Properly handles Optional<User> from userRepository.findByEmail()
     */
    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            String username = userDetails.getUsername();
            
            // ✅ CORRECT: userRepository.findByEmail() returns Optional<User>
            Optional<User> userOpt = userRepository.findByEmail(username);
            
            if (userOpt.isPresent()) {
                return userOpt.get().getId();
            } else {
                throw new RuntimeException("User not found with email: " + username);
            }
        }
        
        throw new RuntimeException("Unable to get current user ID - Not authenticated properly");
    }

    /**
     * Simplified version of getCurrentUserId
     */
    private Long getCurrentUserIdSimple() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + username))
            .getId();
    }

    /**
     * Check if payment exists for order
     */
    public boolean paymentExists(String orderId) {
        return paymentRepository.findByOrderId(orderId).isPresent();
    }

    /**
     * Get all payments (admin only)
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}