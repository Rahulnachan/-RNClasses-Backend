package com.rnclasses.controller;

import com.rnclasses.entity.Course;
import com.rnclasses.entity.User;
import com.rnclasses.repository.CourseRepository;
import com.rnclasses.repository.UserRepository;
import com.rnclasses.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AdminController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // Helper method to get current admin
    private User getCurrentAdmin() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                String email = ((UserDetails) principal).getUsername();
                return userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));
            } else {
                throw new RuntimeException("Invalid authentication principal");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get current admin: " + e.getMessage());
        }
    }

    // ==================== ADMIN STATS ====================

    @GetMapping("/stats")
    public ResponseEntity<?> getAdminStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userRepository.count());
            stats.put("totalCourses", courseRepository.count());
            stats.put("totalEnrollments", enrollmentRepository.count());
            
            // Add course approval stats
            stats.put("pendingCourses", courseRepository.countPendingCourses());
            stats.put("approvedCourses", courseRepository.countApprovedCourses());
            stats.put("rejectedCourses", courseRepository.countRejectedCourses());
            
            // Calculate total revenue (if you have paid courses)
            Double totalRevenue = calculateTotalRevenue();
            stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
            
            // Add more stats
            stats.put("activeUsers", userRepository.countByIsActiveTrue());
            stats.put("publishedCourses", courseRepository.countByPublishedTrue());
            stats.put("students", userRepository.countByRole("STUDENT"));
            stats.put("trainers", userRepository.countByRole("TRAINER"));
            
            return ResponseEntity.ok(Map.of("success", true, "data", stats));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to fetch stats: " + e.getMessage()));
        }
    }

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            
            List<Map<String, Object>> userList = users.stream().map(user -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("name", user.getName());
                map.put("email", user.getEmail());
                map.put("role", user.getRole());
                map.put("isActive", user.isActive());
                map.put("phoneNumber", user.getPhoneNumber());
                map.put("profileImage", user.getProfileImage());
                map.put("createdAt", user.getCreatedAt());
                
                // Add role-specific fields
                if (user.isTrainer()) {
                    map.put("expertise", user.getExpertise());
                    map.put("qualification", user.getQualification());
                    map.put("yearsOfExperience", user.getYearsOfExperience());
                    map.put("bio", user.getBio());
                    map.put("coursesCount", user.getCoursesCreated() != null ? user.getCoursesCreated().size() : 0);
                    map.put("pendingCourses", courseRepository.countPendingCoursesByTrainerId(user.getId()));
                }
                
                if (user.isAdmin()) {
                    map.put("employeeId", user.getEmployeeId());
                    map.put("department", user.getDepartment());
                    map.put("designation", user.getDesignation());
                }
                
                // Add enrollment count
                map.put("enrollmentsCount", user.getEnrollments() != null ? user.getEnrollments().size() : 0);
                
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true, 
                "data", userList,
                "count", userList.size()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to fetch users: " + e.getMessage()));
        }
    }

    // ==================== COURSE MANAGEMENT ====================

    @GetMapping("/courses")
    public ResponseEntity<?> getAllCourses() {
        try {
            List<Course> courses = courseRepository.findAllWithApprovalStatus();
            
            List<Map<String, Object>> courseList = courses.stream().map(course -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", course.getId());
                map.put("title", course.getTitle());
                map.put("description", course.getDescription());
                map.put("longDescription", course.getLongDescription());
                map.put("category", course.getCategory());
                map.put("level", course.getLevel());
                map.put("price", course.getPrice());
                map.put("duration", course.getDuration());
                map.put("imageUrl", course.getImageUrl());
                map.put("image", course.getImageUrl());
                map.put("rating", course.getRating());
                map.put("featured", course.isFeatured());
                map.put("includesCertificate", course.isIncludesCertificate());
                map.put("published", course.isPublished());
                
                // Add approval fields
                map.put("approvalStatus", course.getApprovalStatus());
                map.put("rejectionReason", course.getRejectionReason());
                map.put("approvedAt", course.getApprovedAt());
                
                map.put("studentsCount", course.getStudentsCount());
                map.put("reviewsCount", course.getReviewsCount());
                map.put("createdAt", course.getCreatedAt());
                map.put("updatedAt", course.getUpdatedAt());
                
                // Handle trainer information safely
                if (course.getTrainer() != null) {
                    User trainer = course.getTrainer();
                    Map<String, Object> trainerMap = new HashMap<>();
                    trainerMap.put("id", trainer.getId());
                    trainerMap.put("name", trainer.getName());
                    trainerMap.put("email", trainer.getEmail());
                    trainerMap.put("role", trainer.getRole());
                    trainerMap.put("profileImage", trainer.getProfileImage());
                    trainerMap.put("expertise", trainer.getExpertise());
                    
                    map.put("trainer", trainerMap);
                    map.put("instructor", trainerMap);
                    map.put("instructorName", trainer.getName());
                    map.put("instructorImage", trainer.getProfileImage());
                } else {
                    map.put("trainer", null);
                    map.put("instructor", null);
                    map.put("instructorName", "Not Assigned");
                    map.put("instructorImage", null);
                }
                
                // Get enrollments count
                map.put("students", enrollmentRepository.countByCourseId(course.getId()));
                
                // Handle requirements
                if (course.getRequirements() != null) {
                    map.put("requirements", course.getRequirements());
                } else {
                    map.put("requirements", List.of());
                }
                
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true, 
                "data", courseList,
                "count", courseList.size()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to fetch courses: " + e.getMessage()));
        }
    }

    // ==================== COURSE APPROVAL ENDPOINTS ====================

    @GetMapping("/courses/pending")
    public ResponseEntity<?> getPendingCourses() {
        try {
            List<Course> pendingCourses = courseRepository.findByApprovalStatusOrderByCreatedAtDesc("PENDING");
            
            List<Map<String, Object>> courseList = pendingCourses.stream().map(course -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", course.getId());
                map.put("title", course.getTitle());
                map.put("description", course.getDescription());
                map.put("category", course.getCategory());
                map.put("level", course.getLevel());
                map.put("price", course.getPrice());
                map.put("duration", course.getDuration());
                map.put("imageUrl", course.getImageUrl());
                map.put("createdAt", course.getCreatedAt());
                
                // Trainer info
                if (course.getTrainer() != null) {
                    Map<String, Object> trainerMap = new HashMap<>();
                    trainerMap.put("id", course.getTrainer().getId());
                    trainerMap.put("name", course.getTrainer().getName());
                    trainerMap.put("email", course.getTrainer().getEmail());
                    trainerMap.put("expertise", course.getTrainer().getExpertise());
                    map.put("trainer", trainerMap);
                }
                
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", courseList,
                "count", courseList.size()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PutMapping("/courses/{courseId}/approve")
    public ResponseEntity<?> approveCourse(@PathVariable Long courseId) {
        try {
            User admin = getCurrentAdmin();
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
            
            if (course.isApproved()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Course is already approved"));
            }
            
            course.approve(admin.getId());
            courseRepository.save(course);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Course approved successfully",
                "courseId", courseId
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PutMapping("/courses/{courseId}/reject")
    public ResponseEntity<?> rejectCourse(@PathVariable Long courseId, @RequestBody Map<String, String> request) {
        try {
            User admin = getCurrentAdmin();
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
            
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Rejection reason is required"));
            }
            
            if (course.isRejected()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Course is already rejected"));
            }
            
            course.reject(reason, admin.getId());
            courseRepository.save(course);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Course rejected successfully",
                "courseId", courseId
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/courses/approval-stats")
    public ResponseEntity<?> getApprovalStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("pending", courseRepository.countPendingCourses());
            stats.put("approved", courseRepository.countApprovedCourses());
            stats.put("rejected", courseRepository.countRejectedCourses());
            
            List<Object[]> pendingByTrainer = courseRepository.countPendingCoursesByTrainer();
            List<Map<String, Object>> trainerPending = pendingByTrainer.stream()
                .map(obj -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("trainerId", obj[0]);
                    map.put("trainerName", obj[1]);
                    map.put("pendingCount", obj[2]);
                    return map;
                })
                .collect(Collectors.toList());
            
            stats.put("pendingByTrainer", trainerPending);
            
            return ResponseEntity.ok(Map.of("success", true, "data", stats));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/courses/pending/{courseId}")
    public ResponseEntity<?> getPendingCourseDetails(@PathVariable Long courseId) {
        try {
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
            
            if (!course.isPending()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Course is not in pending state"));
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
            courseMap.put("demoVideo", course.getDemoVideo());
            courseMap.put("featured", course.isFeatured());
            courseMap.put("includesCertificate", course.isIncludesCertificate());
            courseMap.put("requirements", course.getRequirements());
            courseMap.put("createdAt", course.getCreatedAt());
            
            if (course.getTrainer() != null) {
                Map<String, Object> trainerMap = new HashMap<>();
                trainerMap.put("id", course.getTrainer().getId());
                trainerMap.put("name", course.getTrainer().getName());
                trainerMap.put("email", course.getTrainer().getEmail());
                trainerMap.put("expertise", course.getTrainer().getExpertise());
                trainerMap.put("qualification", course.getTrainer().getQualification());
                trainerMap.put("yearsOfExperience", course.getTrainer().getYearsOfExperience());
                trainerMap.put("bio", course.getTrainer().getBio());
                courseMap.put("trainer", trainerMap);
            }
            
            return ResponseEntity.ok(Map.of("success", true, "data", courseMap));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PutMapping("/courses/bulk-approve")
    public ResponseEntity<?> bulkApproveCourses(@RequestBody List<Long> courseIds) {
        try {
            User admin = getCurrentAdmin();
            List<Course> courses = courseRepository.findAllById(courseIds);
            
            int approvedCount = 0;
            for (Course course : courses) {
                if (course.isPending()) {
                    course.approve(admin.getId());
                    approvedCount++;
                }
            }
            
            courseRepository.saveAll(courses);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", approvedCount + " courses approved successfully",
                "approvedCount", approvedCount
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== COURSE CRUD OPERATIONS ====================

    @PostMapping("/courses")
    public ResponseEntity<?> addCourse(@RequestBody Map<String, Object> courseData) {
        try {
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
            if (priceObj instanceof Number) {
                course.setPrice(((Number) priceObj).doubleValue());
            } else if (priceObj instanceof String) {
                try {
                    course.setPrice(Double.parseDouble((String) priceObj));
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
            
            // Handle featured
            Object featuredObj = courseData.get("featured");
            if (featuredObj instanceof Boolean) {
                course.setFeatured((Boolean) featuredObj);
            }
            
            // Handle certificate
            Object certificateObj = courseData.get("includesCertificate");
            if (certificateObj instanceof Boolean) {
                course.setIncludesCertificate((Boolean) certificateObj);
            }
            
            // Handle published - admins can publish directly
            Object publishedObj = courseData.get("published");
            if (publishedObj instanceof Boolean) {
                course.setPublished((Boolean) publishedObj);
                if ((Boolean) publishedObj) {
                    course.setApprovalStatus("APPROVED");
                }
            }
            
            // Handle trainer assignment
            Object trainerIdObj = courseData.get("trainerId");
            if (trainerIdObj != null) {
                Long trainerId = Long.parseLong(trainerIdObj.toString());
                Optional<User> trainerOpt = userRepository.findById(trainerId);
                if (trainerOpt.isPresent() && trainerOpt.get().isTrainer()) {
                    course.setTrainer(trainerOpt.get());
                }
            }
            
            // Handle requirements
            Object requirementsObj = courseData.get("requirements");
            if (requirementsObj instanceof List) {
                course.setRequirements((List<String>) requirementsObj);
            }
            
            // Set timestamps
            course.setCreatedAt(LocalDateTime.now());
            course.setUpdatedAt(LocalDateTime.now());
            
            // Initialize counts
            if (course.getStudentsCount() == null) course.setStudentsCount(0);
            if (course.getReviewsCount() == null) course.setReviewsCount(0);

            Course savedCourse = courseRepository.save(course);
            
            Map<String, Object> responseCourse = new HashMap<>();
            responseCourse.put("id", savedCourse.getId());
            responseCourse.put("title", savedCourse.getTitle());
            responseCourse.put("message", "Course created successfully");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Course added successfully",
                "course", responseCourse
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", "Failed to add course: " + e.getMessage()));
        }
    }

    @PutMapping("/courses/{courseId}")
    public ResponseEntity<?> updateCourse(@PathVariable Long courseId, @RequestBody Map<String, Object> courseData) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

            if (courseData.containsKey("title")) course.setTitle((String) courseData.get("title"));
            if (courseData.containsKey("description")) course.setDescription((String) courseData.get("description"));
            if (courseData.containsKey("longDescription")) course.setLongDescription((String) courseData.get("longDescription"));
            if (courseData.containsKey("category")) course.setCategory((String) courseData.get("category"));
            if (courseData.containsKey("level")) course.setLevel((String) courseData.get("level"));
            if (courseData.containsKey("duration")) course.setDuration((String) courseData.get("duration"));
            
            if (courseData.containsKey("price")) {
                Object priceObj = courseData.get("price");
                if (priceObj instanceof Number) {
                    course.setPrice(((Number) priceObj).doubleValue());
                } else if (priceObj instanceof String) {
                    try {
                        course.setPrice(Double.parseDouble((String) priceObj));
                    } catch (NumberFormatException e) {}
                }
            }
            
            if (courseData.containsKey("imageUrl")) {
                course.setImageUrl((String) courseData.get("imageUrl"));
            } else if (courseData.containsKey("image")) {
                course.setImageUrl((String) courseData.get("image"));
            }
            
            if (courseData.containsKey("featured") && courseData.get("featured") instanceof Boolean) {
                course.setFeatured((Boolean) courseData.get("featured"));
            }
            
            if (courseData.containsKey("includesCertificate") && courseData.get("includesCertificate") instanceof Boolean) {
                course.setIncludesCertificate((Boolean) courseData.get("includesCertificate"));
            }
            
            if (courseData.containsKey("published") && courseData.get("published") instanceof Boolean) {
                course.setPublished((Boolean) courseData.get("published"));
            }
            
            if (courseData.containsKey("trainerId")) {
                Long trainerId = Long.parseLong(courseData.get("trainerId").toString());
                Optional<User> trainerOpt = userRepository.findById(trainerId);
                if (trainerOpt.isPresent() && trainerOpt.get().isTrainer()) {
                    course.setTrainer(trainerOpt.get());
                }
            } else if (courseData.containsKey("trainer")) {
                Map<String, Object> trainerMap = (Map<String, Object>) courseData.get("trainer");
                if (trainerMap != null && trainerMap.containsKey("id")) {
                    Long trainerId = Long.parseLong(trainerMap.get("id").toString());
                    Optional<User> trainerOpt = userRepository.findById(trainerId);
                    if (trainerOpt.isPresent() && trainerOpt.get().isTrainer()) {
                        course.setTrainer(trainerOpt.get());
                    }
                }
            }
            
            if (courseData.containsKey("requirements") && courseData.get("requirements") instanceof List) {
                course.setRequirements((List<String>) courseData.get("requirements"));
            }

            course.setUpdatedAt(LocalDateTime.now());
            courseRepository.save(course);

            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Course updated successfully",
                "courseId", courseId
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", "Failed to update course: " + e.getMessage()));
        }
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseId) {
        try {
            if (!courseRepository.existsById(courseId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "error", "Course not found with id: " + courseId));
            }
            
            courseRepository.deleteById(courseId);
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Course deleted successfully",
                "courseId", courseId
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to delete course: " + e.getMessage()));
        }
    }
    
    // Helper method to calculate total revenue
    private Double calculateTotalRevenue() {
        try {
            Double revenue = enrollmentRepository.getTotalRevenue();
            return revenue != null ? revenue : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}