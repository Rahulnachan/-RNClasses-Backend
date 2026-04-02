package com.rnclasses.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Column(length = 2000)
    private String longDescription;
    
    private String category;
    private String level;
    private String duration;
    private Double price;
    private Double rating;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "demo_video")
    private String demoVideo;
    
    private boolean featured;
    
    @Column(name = "includes_certificate")
    private boolean includesCertificate;
    
    @Column(name = "students_count")
    private Integer studentsCount = 0;
    
    @Column(name = "reviews_count")
    private Integer reviewsCount = 0;
    
    @Column(name = "is_published")
    private boolean published = false; // Changed default to false
    
    // ========== NEW APPROVAL FIELDS ==========
    @Column(name = "approval_status")
    private String approvalStatus = "PENDING"; // PENDING, APPROVED, REJECTED
    
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    // ========================================
    
    @ElementCollection
    @CollectionTable(name = "course_requirements", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "requirement")
    private List<String> requirements = new ArrayList<>();
    
    @ManyToOne
    @JoinColumn(name = "trainer_id")
    @JsonIgnoreProperties({"enrollments", "coursesCreated", "password", "adminRequested", "adminApproved", "createdAt", "updatedAt"})
    private User trainer;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Enrollment> enrollments = new ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Course() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.approvalStatus = "PENDING";
        this.published = false;
    }
    
    // ========== EXISTING GETTERS AND SETTERS ==========
    
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
    
    public User getTrainer() { return trainer; }
    public void setTrainer(User trainer) { this.trainer = trainer; }
    
    @JsonIgnore
    public List<Enrollment> getEnrollments() { return enrollments; }
    public void setEnrollments(List<Enrollment> enrollments) { this.enrollments = enrollments; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // ========== NEW APPROVAL GETTERS AND SETTERS ==========
    
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }
    
    // ========== HELPER METHODS FOR APPROVAL ==========
    
    public boolean isPending() {
        return "PENDING".equals(this.approvalStatus);
    }
    
    public boolean isApproved() {
        return "APPROVED".equals(this.approvalStatus);
    }
    
    public boolean isRejected() {
        return "REJECTED".equals(this.approvalStatus);
    }
    
    public void approve(Long adminId) {
        this.approvalStatus = "APPROVED";
        this.published = true;
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = adminId;
        this.rejectionReason = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void reject(String reason, Long adminId) {
        this.approvalStatus = "REJECTED";
        this.published = false;
        this.rejectionReason = reason;
        this.approvedBy = adminId;
        this.approvedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void resubmit() {
        this.approvalStatus = "PENDING";
        this.published = false;
        this.rejectionReason = null;
        this.approvedBy = null;
        this.approvedAt = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    // ========== EXISTING HELPER METHODS ==========
    
    public String getImage() {
        return this.imageUrl;
    }
    
    public void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    @JsonIgnoreProperties({"enrollments", "coursesCreated", "password", "adminRequested", "adminApproved"})
    public User getInstructor() {
        return this.trainer;
    }
    
    public void setInstructor(User instructor) {
        this.trainer = instructor;
    }
    
    @JsonIgnoreProperties({"enrollments", "coursesCreated", "password", "adminRequested", "adminApproved"})
    public List<User> getAllInstructor() {
        List<User> instructors = new ArrayList<>();
        if (this.trainer != null) {
            instructors.add(this.trainer);
        }
        return instructors;
    }
    
    public String getInstructorImage() {
        return this.trainer != null ? this.trainer.getProfileImage() : null;
    }
    
    public List<String> getAllInstructorImage() {
        List<String> images = new ArrayList<>();
        if (this.trainer != null && this.trainer.getProfileImage() != null) {
            images.add(this.trainer.getProfileImage());
        }
        return images;
    }
    
    public boolean isPublished() {
        return this.published;
    }
    
    public void setPublished(boolean published) {
        this.published = published;
    }
    
    public static List<Course> getAllPublished(List<Course> courses) {
        return courses.stream()
                .filter(Course::isPublished)
                .toList();
    }
    
    public List<String> getRequirements() {
        return this.requirements;
    }
    
    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }
    
    public void addRequirement(String requirement) {
        if (this.requirements == null) {
            this.requirements = new ArrayList<>();
        }
        this.requirements.add(requirement);
    }
    
    // ========== UTILITY METHODS ==========
    
    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", level='" + level + '\'' +
                ", price=" + price +
                ", rating=" + rating +
                ", published=" + published +
                ", approvalStatus='" + approvalStatus + '\'' +
                '}';
    }
}