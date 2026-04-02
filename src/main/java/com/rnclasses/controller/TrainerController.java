package com.rnclasses.controller;

import com.rnclasses.entity.Course;
import com.rnclasses.entity.User;
import com.rnclasses.entity.Enrollment;
import com.rnclasses.repository.CourseRepository;
import com.rnclasses.repository.UserRepository;
import com.rnclasses.repository.EnrollmentRepository;
import com.rnclasses.security.JwtUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainer")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class TrainerController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private JwtUtils jwtUtils;

    private User getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                String email = ((UserDetails) principal).getUsername();
                return userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            } else {
                throw new RuntimeException("Invalid authentication principal");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get current user: " + e.getMessage());
        }
    }

    // ========== GET ALL TRAINERS (PUBLIC) ==========
    @GetMapping("/all")
    public ResponseEntity<?> getAllTrainers() {
        try {
            // Find all users with role TRAINER
            List<User> trainers = userRepository.findByRole("TRAINER");
            
            List<Map<String, Object>> trainerList = trainers.stream().map(trainer -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", trainer.getId());
                map.put("name", trainer.getName());
                map.put("email", trainer.getEmail());
                map.put("profileImage", trainer.getProfileImage());
                map.put("expertise", trainer.getExpertise());
                map.put("qualification", trainer.getQualification());
                map.put("yearsOfExperience", trainer.getYearsOfExperience());
                map.put("bio", trainer.getBio());
                map.put("linkedinUrl", trainer.getLinkedinUrl());
                map.put("githubUrl", trainer.getGithubUrl());
                map.put("phoneNumber", trainer.getPhoneNumber());
                
                // Calculate additional stats (only for approved courses)
                List<Course> trainerCourses = courseRepository.findByTrainerId(trainer.getId());
                List<Course> approvedCourses = trainerCourses.stream()
                    .filter(c -> c.isApproved())
                    .collect(Collectors.toList());
                
                map.put("coursesCount", approvedCourses.size());
                map.put("totalCourses", trainerCourses.size());
                
                // Calculate total students from approved courses only
                long totalStudents = approvedCourses.stream()
                    .mapToLong(c -> enrollmentRepository.countByCourseId(c.getId()))
                    .sum();
                map.put("studentsCount", totalStudents);
                
                // Calculate average rating from approved courses
                double avgRating = approvedCourses.stream()
                    .filter(c -> c.getRating() != null)
                    .mapToDouble(Course::getRating)
                    .average()
                    .orElse(0.0);
                map.put("rating", Math.round(avgRating * 10) / 10.0);
                
                // Add location if available
                if (trainer.getCity() != null && trainer.getCountry() != null) {
                    map.put("location", trainer.getCity() + ", " + trainer.getCountry());
                } else if (trainer.getCity() != null) {
                    map.put("location", trainer.getCity());
                } else {
                    map.put("location", null);
                }
                
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", trainerList,
                "count", trainerList.size()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Failed to fetch trainers: " + e.getMessage()
                ));
        }
    }

    // ========== GET TRAINER COURSES WITH APPROVAL STATUS ==========
    @GetMapping("/courses")
    public ResponseEntity<?> getTrainerCourses() {
        try {
            User trainer = getCurrentUser();
            List<Course> courses = courseRepository.findByTrainerIdWithApprovalStatus(trainer.getId());
            
            List<Map<String, Object>> courseList = courses.stream().map(course -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", course.getId());
                map.put("title", course.getTitle());
                map.put("description", course.getDescription());
                map.put("longDescription", course.getLongDescription());
                map.put("price", course.getPrice());
                map.put("category", course.getCategory());
                map.put("level", course.getLevel());
                map.put("duration", course.getDuration());
                map.put("imageUrl", course.getImageUrl());
                map.put("image", course.getImageUrl());
                map.put("rating", course.getRating());
                map.put("featured", course.isFeatured());
                map.put("includesCertificate", course.isIncludesCertificate());
                map.put("published", course.isPublished());
                
                // 🔥 NEW: Add approval status fields
                map.put("approvalStatus", course.getApprovalStatus());
                map.put("rejectionReason", course.getRejectionReason());
                map.put("approvedAt", course.getApprovedAt());
                
                map.put("studentsCount", enrollmentRepository.countByCourseId(course.getId()));
                map.put("reviewsCount", course.getReviewsCount());
                map.put("createdAt", course.getCreatedAt() != null ? 
                    course.getCreatedAt().toString() : null);
                map.put("updatedAt", course.getUpdatedAt() != null ? 
                    course.getUpdatedAt().toString() : null);
                
                // Handle requirements
                if (course.getRequirements() != null) {
                    map.put("requirements", course.getRequirements());
                } else {
                    map.put("requirements", List.of());
                }
                
                return map;
            }).collect(Collectors.toList());

            // Get counts by status
            long pendingCount = courseList.stream()
                .filter(c -> "PENDING".equals(c.get("approvalStatus")))
                .count();
            long approvedCount = courseList.stream()
                .filter(c -> "APPROVED".equals(c.get("approvalStatus")))
                .count();
            long rejectedCount = courseList.stream()
                .filter(c -> "REJECTED".equals(c.get("approvalStatus")))
                .count();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", courseList,
                "count", courseList.size(),
                "pendingCount", pendingCount,
                "approvedCount", approvedCount,
                "rejectedCount", rejectedCount
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Failed to fetch courses: " + e.getMessage()
                ));
        }
    }

    // ========== GET TRAINER STATS WITH APPROVAL DATA ==========
    @GetMapping("/stats")
    public ResponseEntity<?> getTrainerStats() {
        try {
            User trainer = getCurrentUser();
            List<Course> courses = courseRepository.findByTrainerId(trainer.getId());
            
            long totalCourses = courses.size();
            long totalStudents = 0;
            double totalRating = 0;
            double totalRevenue = 0;
            long publishedCourses = 0;
            long draftCourses = 0;
            
            // 🔥 NEW: Approval stats
            long pendingCourses = 0;
            long approvedCourses = 0;
            long rejectedCourses = 0;

            for (Course course : courses) {
                long studentCount = enrollmentRepository.countByCourseId(course.getId());
                totalStudents += studentCount;
                
                if (course.getRating() != null) {
                    totalRating += course.getRating();
                }
                
                if (course.getPrice() != null && studentCount > 0 && course.isApproved()) {
                    totalRevenue += studentCount * course.getPrice();
                }
                
                if (course.isPublished()) {
                    publishedCourses++;
                } else {
                    draftCourses++;
                }
                
                // Count by approval status
                switch (course.getApprovalStatus()) {
                    case "PENDING":
                        pendingCourses++;
                        break;
                    case "APPROVED":
                        approvedCourses++;
                        break;
                    case "REJECTED":
                        rejectedCourses++;
                        break;
                }
            }

            double avgRating = totalCourses > 0 ? totalRating / totalCourses : 0;

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCourses", totalCourses);
            stats.put("publishedCourses", publishedCourses);
            stats.put("draftCourses", draftCourses);
            
            // 🔥 NEW: Add approval stats
            stats.put("pendingCourses", pendingCourses);
            stats.put("approvedCourses", approvedCourses);
            stats.put("rejectedCourses", rejectedCourses);
            
            stats.put("totalStudents", totalStudents);
            stats.put("avgRating", Math.round(avgRating * 10) / 10.0);
            stats.put("totalRevenue", Math.round(totalRevenue * 100) / 100.0);
            
            // Add monthly trends
            stats.put("monthlyEnrollments", getMonthlyEnrollments(trainer));
            stats.put("popularCourses", getPopularCourses(trainer));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Failed to fetch stats: " + e.getMessage()
                ));
        }
    }

    // ========== CREATE COURSE (with PENDING status) ==========
    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody Map<String, Object> courseData) {
        try {
            User trainer = getCurrentUser();

            // Validate required fields
            if (!courseData.containsKey("title") || courseData.get("title") == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Course title is required"));
            }

            Course course = new Course();
            
            // Basic fields
            course.setTitle((String) courseData.get("title"));
            course.setDescription((String) courseData.get("description"));
            course.setLongDescription((String) courseData.get("longDescription"));
            course.setCategory((String) courseData.get("category"));
            course.setLevel((String) courseData.get("level"));
            course.setDuration((String) courseData.get("duration"));
            
            // Handle price
            Object priceObj = courseData.get("price");
            if (priceObj != null) {
                try {
                    course.setPrice(Double.parseDouble(priceObj.toString()));
                } catch (NumberFormatException e) {
                    course.setPrice(0.0);
                }
            }
            
            // Handle image
            String imageUrl = (String) courseData.get("imageUrl");
            if (imageUrl == null) {
                imageUrl = (String) courseData.get("image");
            }
            course.setImageUrl(imageUrl);
            
            // Handle demo video
            course.setDemoVideo((String) courseData.get("demoVideo"));
            
            // Handle boolean flags
            if (courseData.containsKey("featured")) {
                course.setFeatured(Boolean.TRUE.equals(courseData.get("featured")));
            }
            
            if (courseData.containsKey("includesCertificate")) {
                course.setIncludesCertificate(Boolean.TRUE.equals(courseData.get("includesCertificate")));
            }
            
            // 🔥 IMPORTANT: Set to pending approval
            course.setPublished(false);
            course.setApprovalStatus("PENDING");
            
            // Handle requirements
            if (courseData.containsKey("requirements") && courseData.get("requirements") instanceof List) {
                course.setRequirements((List<String>) courseData.get("requirements"));
            }
            
            // Set trainer and timestamps
            course.setTrainer(trainer);
            course.setCreatedAt(LocalDateTime.now());
            course.setUpdatedAt(LocalDateTime.now());
            
            // Initialize counts
            course.setStudentsCount(0);
            course.setReviewsCount(0);

            Course savedCourse = courseRepository.save(course);
            
            // Prepare response
            Map<String, Object> responseCourse = new HashMap<>();
            responseCourse.put("id", savedCourse.getId());
            responseCourse.put("title", savedCourse.getTitle());
            responseCourse.put("approvalStatus", savedCourse.getApprovalStatus());
            responseCourse.put("message", "Course submitted for admin approval");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Course submitted for admin approval successfully",
                "course", responseCourse
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "success", false,
                    "error", "Failed to create course: " + e.getMessage()
                ));
        }
    }

    // ========== RESUBMIT REJECTED COURSE ==========
    @PutMapping("/courses/{courseId}/resubmit")
    public ResponseEntity<?> resubmitCourse(@PathVariable Long courseId, @RequestBody Map<String, Object> courseData) {
        try {
            User trainer = getCurrentUser();
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

            // Verify this course belongs to the trainer
            if (!course.getTrainer().getId().equals(trainer.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "error", "Not authorized to resubmit this course"));
            }

            // Check if course is rejected
            if (!course.isRejected()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Only rejected courses can be resubmitted"));
            }

            // Update fields if present
            if (courseData.containsKey("title")) course.setTitle((String) courseData.get("title"));
            if (courseData.containsKey("description")) course.setDescription((String) courseData.get("description"));
            if (courseData.containsKey("longDescription")) course.setLongDescription((String) courseData.get("longDescription"));
            if (courseData.containsKey("category")) course.setCategory((String) courseData.get("category"));
            if (courseData.containsKey("level")) course.setLevel((String) courseData.get("level"));
            if (courseData.containsKey("duration")) course.setDuration((String) courseData.get("duration"));
            
            // Handle price
            if (courseData.containsKey("price") && courseData.get("price") != null) {
                try {
                    course.setPrice(Double.parseDouble(courseData.get("price").toString()));
                } catch (NumberFormatException e) {
                    // Keep existing price
                }
            }
            
            // Handle image
            if (courseData.containsKey("imageUrl")) {
                course.setImageUrl((String) courseData.get("imageUrl"));
            } else if (courseData.containsKey("image")) {
                course.setImageUrl((String) courseData.get("image"));
            }
            
            // Handle requirements
            if (courseData.containsKey("requirements") && courseData.get("requirements") instanceof List) {
                course.setRequirements((List<String>) courseData.get("requirements"));
            }
            
            // Resubmit for approval
            course.resubmit();
            courseRepository.save(course);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Course resubmitted for approval successfully",
                "courseId", courseId,
                "approvalStatus", "PENDING"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "success", false,
                    "error", "Failed to resubmit course: " + e.getMessage()
                ));
        }
    }

    // ========== GET REJECTION REASON ==========
    @GetMapping("/courses/{courseId}/rejection-reason")
    public ResponseEntity<?> getRejectionReason(@PathVariable Long courseId) {
        try {
            User trainer = getCurrentUser();
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

            // Verify this course belongs to the trainer
            if (!course.getTrainer().getId().equals(trainer.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "error", "Not authorized to view this course"));
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "isRejected", course.isRejected(),
                "rejectionReason", course.getRejectionReason(),
                "rejectedAt", course.getApprovedAt()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ========== EXISTING ENDPOINTS (updated) ==========

    @GetMapping("/students")
    public ResponseEntity<?> getEnrolledStudents() {
        try {
            User trainer = getCurrentUser();
            List<Course> courses = courseRepository.findByTrainerId(trainer.getId());
            
            // Get all enrollments for trainer's courses (only approved courses)
            List<Enrollment> enrollments = enrollmentRepository.findAll().stream()
                .filter(e -> courses.stream()
                    .anyMatch(c -> c.getId().equals(e.getCourse().getId()) && c.isApproved()))
                .collect(Collectors.toList());
            
            List<Map<String, Object>> students = enrollments.stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", e.getUser().getId());
                    map.put("name", e.getUser().getName());
                    map.put("email", e.getUser().getEmail());
                    map.put("avatar", e.getUser().getProfileImage());
                    map.put("phoneNumber", e.getUser().getPhoneNumber());
                    
                    // Course info
                    map.put("courseId", e.getCourse().getId());
                    map.put("course", e.getCourse().getTitle());
                    map.put("courseImage", e.getCourse().getImageUrl());
                    map.put("courseApprovalStatus", e.getCourse().getApprovalStatus());
                    
                    // Enrollment info
                    map.put("progress", e.getProgress() != null ? e.getProgress() : 0);
                    map.put("status", e.getStatus() != null ? e.getStatus() : "ACTIVE");
                    map.put("joinedDate", e.getEnrolledAt() != null ? 
                        e.getEnrolledAt().toString() : null);
                    map.put("lastAccessed", e.getLastAccessed() != null ? 
                        e.getLastAccessed().toString() : null);
                    map.put("completedAt", e.getCompletedAt() != null ? 
                        e.getCompletedAt().toString() : null);
                    
                    // Derived fields
                    map.put("isActive", "ACTIVE".equals(e.getStatus()));
                    map.put("isCompleted", e.isCompleted());
                    map.put("daysSinceEnrollment", e.getDaysSinceEnrollment());
                    
                    return map;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", students,
                "count", students.size()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Failed to fetch students: " + e.getMessage()
                ));
        }
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<?> getCourseById(@PathVariable Long courseId) {
        try {
            User trainer = getCurrentUser();
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

            // Verify this course belongs to the trainer
            if (!course.getTrainer().getId().equals(trainer.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "error", "Not authorized to view this course"));
            }

            Map<String, Object> courseMap = new HashMap<>();
            courseMap.put("id", course.getId());
            courseMap.put("title", course.getTitle());
            courseMap.put("description", course.getDescription());
            courseMap.put("longDescription", course.getLongDescription());
            courseMap.put("category", course.getCategory());
            courseMap.put("level", course.getLevel());
            courseMap.put("duration", course.getDuration());
            courseMap.put("price", course.getPrice());
            courseMap.put("imageUrl", course.getImageUrl());
            courseMap.put("image", course.getImageUrl());
            courseMap.put("demoVideo", course.getDemoVideo());
            courseMap.put("featured", course.isFeatured());
            courseMap.put("includesCertificate", course.isIncludesCertificate());
            courseMap.put("published", course.isPublished());
            
            // 🔥 NEW: Add approval fields
            courseMap.put("approvalStatus", course.getApprovalStatus());
            courseMap.put("rejectionReason", course.getRejectionReason());
            courseMap.put("approvedAt", course.getApprovedAt());
            
            courseMap.put("rating", course.getRating());
            courseMap.put("studentsCount", enrollmentRepository.countByCourseId(course.getId()));
            courseMap.put("reviewsCount", course.getReviewsCount());
            courseMap.put("requirements", course.getRequirements() != null ? 
                course.getRequirements() : List.of());
            courseMap.put("createdAt", course.getCreatedAt());
            courseMap.put("updatedAt", course.getUpdatedAt());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", courseMap
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to fetch course: " + e.getMessage()));
        }
    }

    // Update a course (only if pending or rejected)
    @PutMapping("/courses/{courseId}")
    public ResponseEntity<?> updateCourse(@PathVariable Long courseId, @RequestBody Map<String, Object> courseData) {
        try {
            User trainer = getCurrentUser();
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

            // Verify this course belongs to the trainer
            if (!course.getTrainer().getId().equals(trainer.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "error", "Not authorized to update this course"));
            }

            // Check if course can be updated (only pending or rejected)
            if (course.isApproved()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Approved courses cannot be updated. Please contact admin."));
            }

            // Update fields if present
            if (courseData.containsKey("title")) course.setTitle((String) courseData.get("title"));
            if (courseData.containsKey("description")) course.setDescription((String) courseData.get("description"));
            if (courseData.containsKey("longDescription")) course.setLongDescription((String) courseData.get("longDescription"));
            if (courseData.containsKey("category")) course.setCategory((String) courseData.get("category"));
            if (courseData.containsKey("level")) course.setLevel((String) courseData.get("level"));
            if (courseData.containsKey("duration")) course.setDuration((String) courseData.get("duration"));
            
            // Handle price
            if (courseData.containsKey("price") && courseData.get("price") != null) {
                try {
                    course.setPrice(Double.parseDouble(courseData.get("price").toString()));
                } catch (NumberFormatException e) {
                    // Keep existing price
                }
            }
            
            // Handle image
            if (courseData.containsKey("imageUrl")) {
                course.setImageUrl((String) courseData.get("imageUrl"));
            } else if (courseData.containsKey("image")) {
                course.setImageUrl((String) courseData.get("image"));
            }
            
            // Handle demo video
            if (courseData.containsKey("demoVideo")) {
                course.setDemoVideo((String) courseData.get("demoVideo"));
            }
            
            // Handle boolean flags
            if (courseData.containsKey("featured")) {
                course.setFeatured(Boolean.TRUE.equals(courseData.get("featured")));
            }
            
            if (courseData.containsKey("includesCertificate")) {
                course.setIncludesCertificate(Boolean.TRUE.equals(courseData.get("includesCertificate")));
            }
            
            // Handle requirements
            if (courseData.containsKey("requirements") && courseData.get("requirements") instanceof List) {
                course.setRequirements((List<String>) courseData.get("requirements"));
            }
            
            // If it was rejected and now updated, reset to pending
            if (course.isRejected()) {
                course.setApprovalStatus("PENDING");
                course.setRejectionReason(null);
            }
            
            course.setUpdatedAt(LocalDateTime.now());
            courseRepository.save(course);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Course updated successfully",
                "courseId", courseId,
                "approvalStatus", course.getApprovalStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "success", false,
                    "error", "Failed to update course: " + e.getMessage()
                ));
        }
    }

    // Delete a course (only if no enrollments)
    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseId) {
        try {
            User trainer = getCurrentUser();
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

            // Verify this course belongs to the trainer
            if (!course.getTrainer().getId().equals(trainer.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "error", "Not authorized to delete this course"));
            }

            // Check if course has enrollments
            long enrollmentCount = enrollmentRepository.countByCourseId(courseId);
            if (enrollmentCount > 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "error", "Cannot delete course with existing enrollments"
                    ));
            }

            courseRepository.delete(course);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Course deleted successfully",
                "courseId", courseId
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Failed to delete course: " + e.getMessage()
                ));
        }
    }
    
    // ========== HELPER METHODS ==========
    
    private List<Map<String, Object>> getMonthlyEnrollments(User trainer) {
        // This would be implemented with actual data from repository
        return List.of(
            Map.of("month", "Jan", "count", 0),
            Map.of("month", "Feb", "count", 0),
            Map.of("month", "Mar", "count", 0)
        );
    }
    
    private List<Map<String, Object>> getPopularCourses(User trainer) {
        List<Course> courses = courseRepository.findByTrainerId(trainer.getId());
        return courses.stream()
            .filter(Course::isApproved) // Only show approved courses in popular list
            .map(course -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", course.getId());
                map.put("title", course.getTitle());
                map.put("students", enrollmentRepository.countByCourseId(course.getId()));
                map.put("approvalStatus", course.getApprovalStatus());
                return map;
            })
            .sorted((a, b) -> Integer.compare(
                (int) b.get("students"), 
                (int) a.get("students")))
            .limit(5)
            .collect(Collectors.toList());
    }
}