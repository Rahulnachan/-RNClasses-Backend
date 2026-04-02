package com.rnclasses.controller;

import com.rnclasses.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        // This would integrate with Razorpay to create an order
        // For now, return a mock response
        Map<String, Object> response = new HashMap<>();
        response.put("id", "order_" + System.currentTimeMillis());
        response.put("amount", request.get("amount"));
        response.put("currency", "INR");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> request) {
        String orderId = request.get("razorpay_order_id");
        String paymentId = request.get("razorpay_payment_id");
        String signature = request.get("razorpay_signature");
        String courseId = request.get("courseId");
        String couponCode = request.get("couponCode");
        String amount = request.get("amount");
        
        Map<String, Object> result = paymentService.processPaymentAndEnrollment(
            orderId, 
            paymentId, 
            Long.parseLong(courseId), 
            couponCode, 
            amount != null ? Integer.parseInt(amount) : 0,
            signature
        );
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory() {
        return ResponseEntity.ok(paymentService.getUserPayments());
    }

    @PostMapping("/apply-coupon")
    public ResponseEntity<?> applyCoupon(@RequestBody Map<String, Object> request) {
        String code = (String) request.get("code");
        int amount = (int) request.get("amount");
        
        Map<String, Object> result = paymentService.applyCoupon(code, amount);
        return ResponseEntity.ok(result);
    }
}