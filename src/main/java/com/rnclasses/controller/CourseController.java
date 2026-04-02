package com.rnclasses.controller;

import com.rnclasses.entity.Course;
import com.rnclasses.entity.User;
import com.rnclasses.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // GET /api/courses - Get all courses
    @GetMapping
    public ResponseEntity<?> getAllCourses() {
        try {
            List<Course> courses = courseService.getAllCourses();
            
            // Convert to DTOs to avoid infinite recursion
            List<CourseDTO> courseDTOs = courses.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", courseDTOs);
            response.put("count", courseDTOs.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to fetch courses: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // GET /api/courses/{id} - Get course by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        try {
            Course course = courseService.getCourseById(id);
            
            // Convert to DTO to avoid infinite recursion
            CourseDTO courseDTO = convertToDTO(course);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", courseDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Convert Course entity to DTO to avoid infinite recursion
     */
    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        
        // Basic fields
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setLongDescription(course.getLongDescription());
        dto.setCategory(course.getCategory());
        dto.setLevel(course.getLevel());
        dto.setDuration(course.getDuration());
        dto.setPrice(course.getPrice());
        dto.setRating(course.getRating());
        dto.setImageUrl(course.getImageUrl());
        dto.setImage(course.getImage()); // Alias method
        dto.setDemoVideo(course.getDemoVideo());
        dto.setFeatured(course.isFeatured());
        dto.setIncludesCertificate(course.isIncludesCertificate());
        dto.setStudentsCount(course.getStudentsCount());
        dto.setReviewsCount(course.getReviewsCount());
        dto.setPublished(course.isPublished());
        dto.setRequirements(course.getRequirements());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        
        // Handle trainer (User) - Convert to simple DTO to avoid recursion
        if (course.getTrainer() != null) {
            TrainerDTO trainerDTO = new TrainerDTO();
            User trainer = course.getTrainer();
            
            trainerDTO.setId(trainer.getId());
            trainerDTO.setName(trainer.getName());
            trainerDTO.setEmail(trainer.getEmail());
            trainerDTO.setRole(trainer.getRole());
            trainerDTO.setPhoneNumber(trainer.getPhoneNumber());
            trainerDTO.setProfileImage(trainer.getProfileImage());
            trainerDTO.setExpertise(trainer.getExpertise());
            trainerDTO.setQualification(trainer.getQualification());
            trainerDTO.setYearsOfExperience(trainer.getYearsOfExperience());
            trainerDTO.setBio(trainer.getBio());
            trainerDTO.setLinkedinUrl(trainer.getLinkedinUrl());
            trainerDTO.setGithubUrl(trainer.getGithubUrl());
            
            dto.setTrainer(trainerDTO);
            dto.setInstructor(trainerDTO); // Alias
        }
        
        // Handle instructor image (for frontend compatibility)
        if (course.getTrainer() != null && course.getTrainer().getProfileImage() != null) {
            dto.setInstructorImage(course.getTrainer().getProfileImage());
        }
        
        // Handle all instructors list
        if (course.getAllInstructor() != null && !course.getAllInstructor().isEmpty()) {
            List<TrainerDTO> allInstructors = course.getAllInstructor().stream()
                    .map(instructor -> {
                        TrainerDTO tDto = new TrainerDTO();
                        tDto.setId(instructor.getId());
                        tDto.setName(instructor.getName());
                        tDto.setEmail(instructor.getEmail());
                        tDto.setProfileImage(instructor.getProfileImage());
                        return tDto;
                    })
                    .collect(Collectors.toList());
            dto.setAllInstructor(allInstructors);
        }
        
        // Handle instructor images
        if (course.getAllInstructorImage() != null) {
            dto.setAllInstructorImage(course.getAllInstructorImage());
        }
        
        // DON'T include enrollments to avoid recursion
        // enrollments are excluded from the DTO
        
        return dto;
    }
    
    // ========== INNER DTO CLASSES ==========
    
    /**
     * DTO for Course to avoid infinite recursion
     */
    public static class CourseDTO {
        private Long id;
        private String title;
        private String description;
        private String longDescription;
        private String category;
        private String level;
        private String duration;
        private Double price;
        private Double rating;
        private String imageUrl;
        private String image; // Alias for frontend
        private String demoVideo;
        private boolean featured;
        private boolean includesCertificate;
        private Integer studentsCount;
        private Integer reviewsCount;
        private boolean published;
        private List<String> requirements;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Trainer related fields
        private TrainerDTO trainer;
        private TrainerDTO instructor; // Alias for frontend
        private String instructorImage;
        private List<TrainerDTO> allInstructor;
        private List<String> allInstructorImage;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getLongDescription() { return longDescription; }
        public void setLongDescription(String longDescription) { this.longDescription = longDescription; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
        
        public String getDemoVideo() { return demoVideo; }
        public void setDemoVideo(String demoVideo) { this.demoVideo = demoVideo; }
        
        public boolean isFeatured() { return featured; }
        public void setFeatured(boolean featured) { this.featured = featured; }
        
        public boolean isIncludesCertificate() { return includesCertificate; }
        public void setIncludesCertificate(boolean includesCertificate) { this.includesCertificate = includesCertificate; }
        
        public Integer getStudentsCount() { return studentsCount; }
        public void setStudentsCount(Integer studentsCount) { this.studentsCount = studentsCount; }
        
        public Integer getReviewsCount() { return reviewsCount; }
        public void setReviewsCount(Integer reviewsCount) { this.reviewsCount = reviewsCount; }
        
        public boolean isPublished() { return published; }
        public void setPublished(boolean published) { this.published = published; }
        
        public List<String> getRequirements() { return requirements; }
        public void setRequirements(List<String> requirements) { this.requirements = requirements; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        
        public TrainerDTO getTrainer() { return trainer; }
        public void setTrainer(TrainerDTO trainer) { this.trainer = trainer; }
        
        public TrainerDTO getInstructor() { return instructor; }
        public void setInstructor(TrainerDTO instructor) { this.instructor = instructor; }
        
        public String getInstructorImage() { return instructorImage; }
        public void setInstructorImage(String instructorImage) { this.instructorImage = instructorImage; }
        
        public List<TrainerDTO> getAllInstructor() { return allInstructor; }
        public void setAllInstructor(List<TrainerDTO> allInstructor) { this.allInstructor = allInstructor; }
        
        public List<String> getAllInstructorImage() { return allInstructorImage; }
        public void setAllInstructorImage(List<String> allInstructorImage) { this.allInstructorImage = allInstructorImage; }
    }
    
    /**
     * DTO for Trainer (simplified User)
     */
    public static class TrainerDTO {
        private Long id;
        private String name;
        private String email;
        private String role;
        private String phoneNumber;
        private String profileImage;
        private String expertise;
        private String qualification;
        private Integer yearsOfExperience;
        private String bio;
        private String linkedinUrl;
        private String githubUrl;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getProfileImage() { return profileImage; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
        
        public String getExpertise() { return expertise; }
        public void setExpertise(String expertise) { this.expertise = expertise; }
        
        public String getQualification() { return qualification; }
        public void setQualification(String qualification) { this.qualification = qualification; }
        
        public Integer getYearsOfExperience() { return yearsOfExperience; }
        public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
        
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        
        public String getLinkedinUrl() { return linkedinUrl; }
        public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
        
        public String getGithubUrl() { return githubUrl; }
        public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
    }
}