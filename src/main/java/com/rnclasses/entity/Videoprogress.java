package com.rnclasses.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "video_progress")
public class Videoprogress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "course_id")
    private Long courseId;
    
    private Integer progress = 0;
    private Boolean completed = false;
    
    @Column(name = "last_watched")
    private LocalDateTime lastWatched;
    
    @Column(name = "total_duration")
    private Integer totalDuration;
    
    @Column(name = "watched_duration")
    private Integer watchedDuration = 0;
    
    @Column(name = "last_video_position")
    private Integer lastVideoPosition = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public LocalDateTime getLastWatched() { return lastWatched; }
    public void setLastWatched(LocalDateTime lastWatched) { this.lastWatched = lastWatched; }

    public Integer getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Integer totalDuration) { this.totalDuration = totalDuration; }

    public Integer getWatchedDuration() { return watchedDuration; }
    public void setWatchedDuration(Integer watchedDuration) { this.watchedDuration = watchedDuration; }

    public Integer getLastVideoPosition() { return lastVideoPosition; }
    public void setLastVideoPosition(Integer lastVideoPosition) { this.lastVideoPosition = lastVideoPosition; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}