package com.rnclasses.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Enrollment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"enrollments", "coursesCreated", "password", "adminRequested", "adminApproved", "createdAt", "updatedAt"})
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"enrollments", "trainer", "requirements", "longDescription", "createdAt", "updatedAt"})
    private Course course;
    
    @Column(nullable = false)
    private Integer progress = 0;
    
    private String status; // ACTIVE, COMPLETED, CANCELLED
    
    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;
    
    // Constructors
    public Enrollment() {
        this.enrolledAt = LocalDateTime.now();
        this.lastAccessed = LocalDateTime.now();
        this.status = "ACTIVE";
    }
    
    public Enrollment(User user, Course course) {
        this.user = user;
        this.course = course;
        this.enrolledAt = LocalDateTime.now();
        this.lastAccessed = LocalDateTime.now();
        this.status = "ACTIVE";
        this.progress = 0;
    }
    
    // ========== GETTERS AND SETTERS WITH JSON IGNORES ==========
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    @JsonIgnoreProperties({"enrollments", "coursesCreated", "password", "adminRequested", "adminApproved"})
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    @JsonIgnoreProperties({"enrollments", "trainer", "requirements", "longDescription"})
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public LocalDateTime getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Alias for getLastAccessed() - fixes "getLastAccessedAt() is undefined"
     */
    public LocalDateTime getLastAccessedAt() {
        return this.lastAccessed;
    }
    
    /**
     * Alias for setLastAccessed() - fixes "setLastAccessedAt() is undefined"
     */
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessed = lastAccessedAt;
    }
    
    /**
     * Check if enrollment is active
     */
    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }
    
    /**
     * Check if enrollment is completed
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(this.status) || (this.progress != null && this.progress >= 100);
    }
    
    /**
     * Mark enrollment as completed
     */
    public void markAsCompleted() {
        this.status = "COMPLETED";
        this.progress = 100;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * Update last accessed time
     */
    public void updateLastAccessed() {
        this.lastAccessed = LocalDateTime.now();
    }
    
    /**
     * Get days since enrollment
     */
    public long getDaysSinceEnrollment() {
        if (this.enrolledAt == null) return 0;
        return java.time.Duration.between(this.enrolledAt, LocalDateTime.now()).toDays();
    }
    
    /**
     * Get days since last access
     */
    public long getDaysSinceLastAccess() {
        if (this.lastAccessed == null) return 0;
        return java.time.Duration.between(this.lastAccessed, LocalDateTime.now()).toDays();
    }
    
    /**
     * Get completion percentage
     */
    public double getCompletionPercentage() {
        return this.progress != null ? this.progress : 0;
    }
    
    /**
     * Update progress
     */
    public void updateProgress(Integer newProgress) {
        this.progress = newProgress;
        this.lastAccessed = LocalDateTime.now();
        if (newProgress >= 100) {
            this.status = "COMPLETED";
            this.completedAt = LocalDateTime.now();
        }
    }
    
    @Override
    public String toString() {
        return "Enrollment{" +
                "id=" + id +
                ", user=" + (user != null ? user.getEmail() : "null") +
                ", course=" + (course != null ? course.getTitle() : "null") +
                ", progress=" + progress +
                ", status='" + status + '\'' +
                '}';
    }
}