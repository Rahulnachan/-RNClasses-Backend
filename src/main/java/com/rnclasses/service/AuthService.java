package com.rnclasses.service;

import com.rnclasses.dto.LoginRequest;
import com.rnclasses.dto.LoginResponse;
import com.rnclasses.dto.RegisterRequest;
import com.rnclasses.entity.User;
import com.rnclasses.repository.UserRepository;
import com.rnclasses.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // 🔥 SUPER ADMIN EMAIL
    private static final String SUPER_ADMIN_EMAIL = "nachanr99@gmail.com";

    public Map<String, Object> register(RegisterRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            response.put("success", false);
            response.put("message", "Email already exists");
            return response;
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        
        // 🔥 ROLE VALIDATION LOGIC
        String requestedRole = request.getRole();
        
        // Check if someone is trying to become admin
        if ("ADMIN".equals(requestedRole)) {
            // Only SUPER_ADMIN_EMAIL can become admin directly
            if (SUPER_ADMIN_EMAIL.equals(request.getEmail())) {
                user.setRole("ADMIN");
                user.setEmployeeId(request.getEmployeeId() != null ? request.getEmployeeId() : "SUPER001");
                user.setDepartment(request.getDepartment() != null ? request.getDepartment() : "Administration");
                user.setDesignation(request.getDesignation() != null ? request.getDesignation() : "Super Admin");
                user.setJoiningDate(request.getJoiningDate() != null ? request.getJoiningDate() : LocalDateTime.now());
                System.out.println("✅ SUPER ADMIN created: " + request.getEmail());
            } else {
                response.put("success", false);
                response.put("message", "Admin accounts cannot be created directly.");
                return response;
            }
        } 
        // Handle TRAINER role
        else if ("TRAINER".equals(requestedRole)) {
            user.setRole("TRAINER");
            user.setExpertise(request.getExpertise());
            user.setQualification(request.getQualification());
            user.setYearsOfExperience(request.getYearsOfExperience());
            user.setBio(request.getBio());
            user.setLinkedinUrl(request.getLinkedinUrl());
            user.setGithubUrl(request.getGithubUrl());
        } 
        // Default to STUDENT
        else {
            user.setRole("STUDENT");
            user.setAddress(request.getAddress());
            user.setCity(request.getCity());
            user.setState(request.getState());
            user.setCountry(request.getCountry());
            user.setPincode(request.getPincode());
            user.setDateOfBirth(request.getDateOfBirth());
            user.setGender(request.getGender());
        }
        
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Save user
        User savedUser = userRepository.save(user);

        response.put("success", true);
        response.put("message", "Registered Successfully");
        response.put("role", savedUser.getRole());
        response.put("email", savedUser.getEmail());
        response.put("name", savedUser.getName());
        
        System.out.println("✅ User registered: " + savedUser.getEmail() + " with role: " + savedUser.getRole());
        
        return response;
    }

    public LoginResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Check if user is active
        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated. Please contact admin.");
        }

        // Generate JWT token with role
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // Create and return COMPLETE LoginResponse with ALL user details
        LoginResponse response = new LoginResponse();
        
        // Set basic fields
        response.setToken(token);
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setPhone(user.getPhoneNumber());
        response.setActive(user.isActive());
        response.setProfileImage(user.getProfileImage());

        // Set common fields
        response.setAddress(user.getAddress());
        response.setCity(user.getCity());
        response.setState(user.getState());
        response.setCountry(user.getCountry());
        response.setPincode(user.getPincode());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setGender(user.getGender());

        // Set trainer-specific fields
        if (user.isTrainer()) {
            response.setExpertise(user.getExpertise());
            response.setQualification(user.getQualification());
            response.setYearsOfExperience(user.getYearsOfExperience());
            response.setBio(user.getBio());
            response.setLinkedinUrl(user.getLinkedinUrl());
            response.setGithubUrl(user.getGithubUrl());
        }

        // Set admin-specific fields
        if (user.isAdmin()) {
            response.setEmployeeId(user.getEmployeeId());
            response.setDepartment(user.getDepartment());
            response.setDesignation(user.getDesignation());
            response.setJoiningDate(user.getJoiningDate());
        }

        System.out.println("✅ Login - User: " + user.getEmail() + 
                           ", Role: " + user.getRole());

        return response;
    }

    // ========== 🔥 NEW: Handle Google OAuth Login with Code ==========
    @Transactional
    public LoginResponse handleGoogleLogin(String code) {
        // In a real implementation, you would exchange the code for user info
        // For demo purposes, we'll simulate with mock data
        // This should be replaced with actual Google API calls
        
        try {
            // Simulate getting user info from Google
            String email = "google.user@gmail.com"; // This would come from Google
            String name = "Google User"; // This would come from Google
            String picture = "https://lh3.googleusercontent.com/a/default-user"; // This would come from Google
            
            return handleGoogleUser(email, name, picture);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process Google login: " + e.getMessage());
        }
    }

    // ========== 🔥 NEW: Handle Google User Data ==========
    @Transactional
    public LoginResponse handleGoogleUser(String email, String name, String picture) {
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;
        
        if (existingUser.isPresent()) {
            // User exists - update information if needed
            user = existingUser.get();
            user.setName(name);
            if (picture != null) {
                user.setProfileImage(picture);
            }
            user.setUpdatedAt(LocalDateTime.now());
        } else {
            // New user - create account
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setProfileImage(picture);
            user.setRole("STUDENT"); // Default role for Google users
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            // Generate a random password (user will never use it)
            String randomPassword = UUID.randomUUID().toString();
            user.setPassword(passwordEncoder.encode(randomPassword));
        }
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole());
        
        // Create login response
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole());
        response.setPhone(savedUser.getPhoneNumber());
        response.setActive(savedUser.isActive());
        response.setProfileImage(savedUser.getProfileImage());
        
        // Set common fields
        response.setAddress(savedUser.getAddress());
        response.setCity(savedUser.getCity());
        response.setState(savedUser.getState());
        response.setCountry(savedUser.getCountry());
        response.setPincode(savedUser.getPincode());
        response.setDateOfBirth(savedUser.getDateOfBirth());
        response.setGender(savedUser.getGender());

        // Set trainer-specific fields if applicable
        if (savedUser.isTrainer()) {
            response.setExpertise(savedUser.getExpertise());
            response.setQualification(savedUser.getQualification());
            response.setYearsOfExperience(savedUser.getYearsOfExperience());
            response.setBio(savedUser.getBio());
            response.setLinkedinUrl(savedUser.getLinkedinUrl());
            response.setGithubUrl(savedUser.getGithubUrl());
        }

        // Set admin-specific fields if applicable
        if (savedUser.isAdmin()) {
            response.setEmployeeId(savedUser.getEmployeeId());
            response.setDepartment(savedUser.getDepartment());
            response.setDesignation(savedUser.getDesignation());
            response.setJoiningDate(savedUser.getJoiningDate());
        }

        System.out.println("✅ Google Login - User: " + savedUser.getEmail() + 
                           ", Role: " + savedUser.getRole());

        return response;
    }

    public String loginToken(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        // Generate token with role
        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }
}