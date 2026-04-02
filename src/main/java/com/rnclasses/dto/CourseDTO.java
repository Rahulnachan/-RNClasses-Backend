package com.rnclasses.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private String longDescription;
    private String instructor;
    private String instructorImage;
    private String instructorBio;
    private String category;
    private String level;
    private Double price;
    private Double rating;
    private Integer studentsCount;  // Changed from students to studentsCount
    private String duration;
    private String imageUrl;  // Changed from image to imageUrl
    private String demoVideo;
    private boolean featured;
    private boolean includesCertificate;
    private List<String> whatYouWillLearn;
    private List<String> requirements;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean published;  // Changed from isPublished to published

    // Constructors
    public CourseDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLongDescription() { return longDescription; }
    public void setLongDescription(String longDescription) { this.longDescription = longDescription; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public String getInstructorImage() { return instructorImage; }
    public void setInstructorImage(String instructorImage) { this.instructorImage = instructorImage; }

    public String getInstructorBio() { return instructorBio; }
    public void setInstructorBio(String instructorBio) { this.instructorBio = instructorBio; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getStudentsCount() { return studentsCount; }
    public void setStudentsCount(Integer studentsCount) { this.studentsCount = studentsCount; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDemoVideo() { return demoVideo; }
    public void setDemoVideo(String demoVideo) { this.demoVideo = demoVideo; }

    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }

    public boolean isIncludesCertificate() { return includesCertificate; }
    public void setIncludesCertificate(boolean includesCertificate) { this.includesCertificate = includesCertificate; }

    public List<String> getWhatYouWillLearn() { return whatYouWillLearn; }
    public void setWhatYouWillLearn(List<String> whatYouWillLearn) { this.whatYouWillLearn = whatYouWillLearn; }

    public List<String> getRequirements() { return requirements; }
    public void setRequirements(List<String> requirements) { this.requirements = requirements; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
}