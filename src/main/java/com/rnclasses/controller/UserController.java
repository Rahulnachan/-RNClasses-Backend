package com.rnclasses.controller;

import com.rnclasses.entity.User;
import com.rnclasses.repository.UserRepository;
import com.rnclasses.service.EnrollmentService;
import com.rnclasses.security.JwtUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class UserController {

    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;
    
    @Autowired(required = false)
    private JwtUtils jwtUtils;

    public UserController(UserRepository userRepository,
                          EnrollmentService enrollmentService) {
        this.userRepository = userRepository;
        this.enrollmentService = enrollmentService;
    }

    /**
     * Get current authenticated user from SecurityContext
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        String email;
        
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /**
     * Upload profile photo - COMPLETE VERSION WITH DETAILED LOGGING
     */
    @PostMapping("/upload-profile-photo")
    public ResponseEntity<?> uploadProfilePhoto(@RequestParam("profileImage") MultipartFile file) {
        System.out.println("\n=========================================");
        System.out.println("📸 PHOTO UPLOAD REQUEST RECEIVED");
        System.out.println("=========================================");
        
        try {
            // Step 1: Check authentication
            System.out.println("🔐 Step 1: Checking authentication...");
            User user = getCurrentUser();
            System.out.println("✅ User authenticated: " + user.getEmail() + " (ID: " + user.getId() + ")");
            
            // Step 2: Check file
            System.out.println("\n📁 Step 2: Checking file...");
            System.out.println("   Filename: " + file.getOriginalFilename());
            System.out.println("   Size: " + file.getSize() + " bytes (" + (file.getSize() / 1024) + " KB)");
            System.out.println("   Content Type: " + file.getContentType());
            
            if (file.isEmpty()) {
                System.out.println("❌ File is empty!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Please select a file"));
            }

            // Step 3: Validate file type
            System.out.println("\n🔍 Step 3: Validating file type...");
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                System.out.println("❌ Invalid file type: " + contentType);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Only image files are allowed (JPG, PNG, GIF)"));
            }
            System.out.println("✅ File type valid: " + contentType);

            // Step 4: Validate file size (max 5MB)
            System.out.println("\n📏 Step 4: Validating file size...");
            if (file.getSize() > 5 * 1024 * 1024) {
                System.out.println("❌ File too large: " + file.getSize() + " bytes");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "File size must be less than 5MB"));
            }
            System.out.println("✅ File size valid");

            // Step 5: Create upload directory
            System.out.println("\n📂 Step 5: Setting up upload directory...");
            String uploadDir = "uploads/profile-photos/";
            Path uploadPath = Paths.get(uploadDir);
            System.out.println("   Path: " + uploadPath.toAbsolutePath());
            
            try {
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                    System.out.println("✅ Created upload directory");
                } else {
                    System.out.println("✅ Upload directory already exists");
                }
            } catch (IOException e) {
                System.err.println("❌ Failed to create upload directory: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Failed to create upload directory: " + e.getMessage()));
            }

            // Step 6: Generate unique filename
            System.out.println("\n📝 Step 6: Generating filename...");
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = System.currentTimeMillis() + "_" + user.getId() + fileExtension;
            System.out.println("   Original: " + originalFilename);
            System.out.println("   New: " + fileName);
            
            // Step 7: Save file
            System.out.println("\n💾 Step 7: Saving file...");
            Path filePath = uploadPath.resolve(fileName);
            try {
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("✅ File saved: " + filePath.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("❌ Failed to save file: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Failed to save file: " + e.getMessage()));
            }
            
            // Step 8: Delete old profile image if exists
            System.out.println("\n🗑️ Step 8: Checking for old profile photo...");
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                try {
                    String oldFileName = user.getProfileImage().substring(user.getProfileImage().lastIndexOf("/") + 1);
                    Path oldFilePath = uploadPath.resolve(oldFileName);
                    if (Files.exists(oldFilePath)) {
                        Files.delete(oldFilePath);
                        System.out.println("✅ Deleted old photo: " + oldFileName);
                    }
                } catch (IOException e) {
                    System.err.println("⚠️ Failed to delete old photo: " + e.getMessage());
                }
            } else {
                System.out.println("ℹ️ No old photo to delete");
            }
            
            // Step 9: Update database
            System.out.println("\n💾 Step 9: Updating database...");
            String imageUrl = "/uploads/profile-photos/" + fileName;
            user.setProfileImage(imageUrl);
            userRepository.save(user);
            System.out.println("✅ Database updated with URL: " + imageUrl);
            
            // Step 10: Return success response
            System.out.println("\n✅ UPLOAD COMPLETE!");
            System.out.println("=========================================\n");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("message", "Profile photo uploaded successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (ResponseStatusException e) {
            System.err.println("❌ Authentication error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to upload photo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            User user = getCurrentUser();
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("name", user.getName());
            profile.put("email", user.getEmail());
            profile.put("role", user.getRole());
            profile.put("createdAt", user.getCreatedAt());
            
            if (user.getPhoneNumber() != null) {
                profile.put("phoneNumber", user.getPhoneNumber());
            }
            
            if (user.getProfileImage() != null) {
                profile.put("profileImage", user.getProfileImage());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", profile);
            
            return ResponseEntity.ok(response);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Update user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> updates) {
        try {
            User user = getCurrentUser();
            
            if (updates.containsKey("name")) {
                user.setName((String) updates.get("name"));
            }
            
            if (updates.containsKey("phoneNumber")) {
                user.setPhoneNumber((String) updates.get("phoneNumber"));
            }
            
            if (updates.containsKey("profileImage")) {
                user.setProfileImage((String) updates.get("profileImage"));
            }
            
            User updatedUser = userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("name", updatedUser.getName());
            response.put("email", updatedUser.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get user profile by token
     */
    @GetMapping("/profile/token")
    public ResponseEntity<?> getUserProfileByToken(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtils.extractEmail(jwt);
            
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "error", "User not found"));
            }
            
            User user = userOptional.get();
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("name", user.getName());
            profile.put("email", user.getEmail());
            profile.put("role", user.getRole());
            profile.put("createdAt", user.getCreatedAt());
            
            if (user.getPhoneNumber() != null) {
                profile.put("phoneNumber", user.getPhoneNumber());
            }
            
            if (user.getProfileImage() != null) {
                profile.put("profileImage", user.getProfileImage());
            }
            
            return ResponseEntity.ok(Map.of("success", true, "data", profile));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", "Invalid token: " + e.getMessage()));
        }
    }

    /**
     * Enroll in a course
     */
    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<?> enrollCourse(@PathVariable Long courseId) {
        try {
            User currentUser = getCurrentUser();
            
            Map<String, Object> enrollmentResult = enrollmentService.enroll(currentUser, courseId);
            
            if (enrollmentResult.containsKey("success") && !(Boolean)enrollmentResult.get("success")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(enrollmentResult);
            }
            
            return ResponseEntity.ok(enrollmentResult);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get current user's enrolled courses
     */
    @GetMapping("/my-courses")
    public ResponseEntity<?> getMyCourses() {
        try {
            User currentUser = getCurrentUser();
            List<Map<String, Object>> coursesWithDetails = enrollmentService.getMyCoursesWithDetails(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", coursesWithDetails);
            response.put("count", coursesWithDetails.size());
            response.put("user", currentUser.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get enrollment progress for a specific course
     */
    @GetMapping("/progress/{courseId}")
    public ResponseEntity<?> getEnrollmentProgress(@PathVariable Long courseId) {
        try {
            User currentUser = getCurrentUser();
            Map<String, Object> progress = enrollmentService.getEnrollmentProgress(currentUser, courseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", progress);
            
            return ResponseEntity.ok(response);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Update course progress
     */
    @PutMapping("/progress/{courseId}")
    public ResponseEntity<?> updateProgress(@PathVariable Long courseId, 
                                           @RequestBody Map<String, Integer> progressData) {
        try {
            User currentUser = getCurrentUser();
            Integer progress = progressData.get("progress");
            
            if (progress == null) {
                throw new RuntimeException("Progress value is required");
            }
            
            Map<String, Object> result = enrollmentService.updateProgress(currentUser, courseId, progress);
            
            if (result.containsKey("success") && !(Boolean)result.get("success")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get user enrollment statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            User currentUser = getCurrentUser();
            Map<String, Object> stats = enrollmentService.getUserEnrollmentStats(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Cancel enrollment
     */
    @DeleteMapping("/enroll/{courseId}")
    public ResponseEntity<?> cancelEnrollment(@PathVariable Long courseId) {
        try {
            User currentUser = getCurrentUser();
            Map<String, Object> result = enrollmentService.cancelEnrollment(currentUser, courseId);
            
            if (result.containsKey("success") && !(Boolean)result.get("success")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}