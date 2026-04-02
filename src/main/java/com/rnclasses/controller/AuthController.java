package com.rnclasses.controller;

import com.rnclasses.dto.LoginRequest;
import com.rnclasses.dto.LoginResponse;
import com.rnclasses.dto.RegisterRequest;
import com.rnclasses.entity.User;
import com.rnclasses.service.AuthService;
import com.rnclasses.service.OTPService;
import com.rnclasses.security.JwtUtils;
import com.rnclasses.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;
    
    @Autowired
    private OTPService otpService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserRepository userRepository;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            Map<String, Object> response = authService.register(request);
            
            if (response.containsKey("success") && !(Boolean)response.get("success")) {
                return ResponseEntity.badRequest().body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            System.out.println("✅ Login Response - Role: " + response.getRole());
            System.out.println("✅ Login Response - Name: " + response.getName());
            System.out.println("✅ Login Response - Email: " + response.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(401).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ========== 🔥 NEW: Google OAuth Callback Endpoint ==========
    @GetMapping("/oauth2/callback")
    public ResponseEntity<?> oauth2Callback(@RequestParam String code) {
        try {
            // In a real implementation, you would exchange the code for tokens
            // For now, we'll simulate a successful Google login
            LoginResponse response = authService.handleGoogleLogin(code);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Google login failed: " + e.getMessage()
            ));
        }
    }

    // ========== 🔥 NEW: Google Login Success Handler ==========
    @GetMapping("/oauth2/success")
    public ResponseEntity<?> oauth2Success(@AuthenticationPrincipal OAuth2User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "No user principal found"
                ));
            }

            String email = principal.getAttribute("email");
            String name = principal.getAttribute("name");
            String picture = principal.getAttribute("picture");

            LoginResponse response = authService.handleGoogleUser(email, name, picture);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Google login failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtils.extractEmail(token.replace("Bearer ", ""));
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("name", user.getName());
            profile.put("email", user.getEmail());
            profile.put("role", user.getRole());
            profile.put("phone", user.getPhoneNumber());
            profile.put("active", user.isActive());
            profile.put("profileImage", user.getProfileImage());
            
            return ResponseEntity.ok(profile);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String identifier = request.get("identifier");
            String method = request.get("method");
            
            User user = userRepository.findByEmail(identifier)
                    .orElse(null);
                    
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with this email"
                ));
            }
            
            String otp = otpService.generateOTP();
            otpService.saveOTP(identifier, otp, method.toUpperCase());
            
            if ("email".equalsIgnoreCase(method)) {
                otpService.sendEmailOTP(identifier, otp);
            } else {
                otpService.sendMobileOTP(identifier, otp);
            }
            
            System.out.println("📧 OTP sent to " + identifier + ": " + otp);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OTP sent successfully"
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String identifier = request.get("identifier");
            String otp = request.get("otp");
            String newPassword = request.get("newPassword");
            String method = request.get("method");
            
            boolean isOtpValid = otpService.verifyOTPForReset(identifier, otp, method.toUpperCase());
            
            if (!isOtpValid) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid or expired OTP"
                ));
            }
            
            User user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            otpService.removeOTP(identifier, method.toUpperCase());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password reset successfully"
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}