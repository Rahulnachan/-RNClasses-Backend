package com.rnclasses.controller;

import com.rnclasses.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/newsletter")
@CrossOrigin(origins = "http://localhost:5173")
public class NewsletterController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribeToNewsletter(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email is required"));
            }
            
            // Simple email validation
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid email format"));
            }
            
            emailService.subscribeToNewsletter(email);
            return ResponseEntity.ok(Map.of("success", true, "message", "Subscribed successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}