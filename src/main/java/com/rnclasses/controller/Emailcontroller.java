package com.rnclasses.controller;

import com.rnclasses.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "http://localhost:5173")
public class Emailcontroller {

    @Autowired
    private EmailService emailService;

    @PostMapping("/welcome")
    public ResponseEntity<?> sendWelcomeEmail(@RequestBody Map<String, String> request) {
        emailService.sendWelcomeEmail(request.get("email"), request.get("name"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/enrollment")
    public ResponseEntity<?> sendEnrollmentEmail(@RequestBody Map<String, String> request) {
        emailService.sendEnrollmentEmail(request.get("email"), request.get("course"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/receipt")
    public ResponseEntity<?> sendPaymentReceipt(@RequestBody Map<String, Object> request) {
        emailService.sendPaymentReceipt((String) request.get("email"), request);
        return ResponseEntity.ok(Map.of("success", true));
    }
}