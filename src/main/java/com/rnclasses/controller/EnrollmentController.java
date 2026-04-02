package com.rnclasses.controller;

import com.rnclasses.entity.Course;
import com.rnclasses.entity.User;
import com.rnclasses.repository.UserRepository;
import com.rnclasses.service.EnrollmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;

    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService,
                                UserRepository userRepository) {
        this.enrollmentService = enrollmentService;
        this.userRepository = userRepository;
    }

    /**
     * Helper method to get current authenticated user
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        String email = authentication.getName();
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /**
     * Enroll in a course
     */
    @PostMapping("/{courseId}")
    public ResponseEntity<?> enroll(@PathVariable Long courseId,
                                   Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            Map<String, Object> result = enrollmentService.enroll(user, courseId);
            
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

    /**
     * Get user's enrolled courses with details
     */
    @GetMapping("/my-courses")
    public ResponseEntity<?> getMyCourses(Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            List<Map<String, Object>> courses = enrollmentService.getMyCoursesWithDetails(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", courses);
            response.put("count", courses.size());
            
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
     * Get progress for a specific course
     */
    @GetMapping("/progress/{courseId}")
    public ResponseEntity<?> getProgress(@PathVariable Long courseId,
                                       Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            Map<String, Object> progress = enrollmentService.getEnrollmentProgress(user, courseId);
            
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
     * Update progress
     */
    @PutMapping("/progress/{courseId}")
    public ResponseEntity<?> updateProgress(@PathVariable Long courseId,
                                          @RequestBody Map<String, Integer> progressData,
                                          Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            Integer progress = progressData.get("progress");
            
            if (progress == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Progress value is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Map<String, Object> result = enrollmentService.updateProgress(user, courseId, progress);
            
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
     * Get enrollment statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            Map<String, Object> stats = enrollmentService.getUserEnrollmentStats(user);
            
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
    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> cancelEnrollment(@PathVariable Long courseId,
                                            Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            Map<String, Object> result = enrollmentService.cancelEnrollment(user, courseId);
            
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

    /**
     * Check if user is enrolled in a course
     */
    @GetMapping("/check/{courseId}")
    public ResponseEntity<?> checkEnrollment(@PathVariable Long courseId,
                                           Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            boolean isEnrolled = enrollmentService.isEnrolled(user, courseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isEnrolled", isEnrolled);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}