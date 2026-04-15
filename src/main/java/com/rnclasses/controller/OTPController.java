package com.rnclasses.controller;

import com.rnclasses.service.OTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:3000",
    "https://rn-classes-two.vercel.app",
    "https://rn-classes-l8l1rp19a-rahul-nachans-projects.vercel.app"
})
public class OTPController {

    @Autowired
    private OTPService otpService;

    @PostMapping("/send-email")
    public ResponseEntity<?> sendEmailOTP(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = otpService.generateOTP();
        otpService.saveOTP(email, otp, "EMAIL");
        
        // Send email with OTP
        otpService.sendEmailOTP(email, otp);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OTP sent successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-mobile")
    public ResponseEntity<?> sendMobileOTP(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String otp = otpService.generateOTP();
        otpService.saveOTP(phone, otp, "MOBILE");
        
        // Send SMS with OTP (integrate with SMS service)
        otpService.sendMobileOTP(phone, otp);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OTP sent successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, String> request) {
        String identifier = request.get("identifier");
        String otp = request.get("otp");
        String type = request.get("type");
        
        boolean isValid = otpService.verifyOTP(identifier, otp, type);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", isValid);
        response.put("message", isValid ? "OTP verified" : "Invalid OTP");
        return ResponseEntity.ok(response);
    }
}