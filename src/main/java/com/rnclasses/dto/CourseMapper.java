package com.rnclasses.dto;

import com.rnclasses.entity.Course;
import com.rnclasses.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class CourseMapper {

    public static CourseDTO toDTO(Course course) {
        if (course == null) {
            return null;
        }

        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setLongDescription(course.getLongDescription());
        
        // ✅ FIXED: Use getTrainer() instead of getInstructor()
        if (course.getTrainer() != null) {
            User trainer = course.getTrainer();
            dto.setInstructor(trainer.getName());
            dto.setInstructorImage(trainer.getProfileImage());
            dto.setInstructorBio(trainer.getBio());
        }
        
        dto.setCategory(course.getCategory());
        dto.setLevel(course.getLevel());
        dto.setPrice(course.getPrice());
        dto.setRating(course.getRating());
        
        // ✅ FIXED: Use correct method names from your Course.java
        dto.setStudentsCount(course.getStudentsCount());  // not getStudents()
        dto.setDuration(course.getDuration());
        dto.setImageUrl(course.getImageUrl());  // not getImage()
        dto.setDemoVideo(course.getDemoVideo());
        dto.setFeatured(course.isFeatured());
        dto.setIncludesCertificate(course.isIncludesCertificate());
        
        // ✅ FIXED: getRequirements() exists in your Course.java
        dto.setRequirements(course.getRequirements());
        
        // Note: getWhatYouWillLearn() doesn't exist in your Course.java
        // You need to either:
        // 1. Add this field to Course.java, or
        // 2. Remove it from DTO, or
        // 3. Set empty list for now
        dto.setWhatYouWillLearn(List.of()); // Temporary fix
        
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        
        // ✅ FIXED: isPublished() exists in your Course.java
        dto.setPublished(course.isPublished());  // not getIsPublished()
        
        return dto;
    }

    public static List<CourseDTO> toDTOList(List<Course> courses) {
        return courses.stream()
                .map(CourseMapper::toDTO)
                .collect(Collectors.toList());
    }

    public static Course toEntity(CourseDTO dto) {
        if (dto == null) {
            return null;
        }

        Course course = new Course();
        course.setId(dto.getId());
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setLongDescription(dto.getLongDescription());
        
        // Note: Trainer needs to be set separately (fetch from DB)
        
        course.setCategory(dto.getCategory());
        course.setLevel(dto.getLevel());
        course.setPrice(dto.getPrice());
        course.setRating(dto.getRating());
        course.setStudentsCount(dto.getStudentsCount());
        course.setDuration(dto.getDuration());
        course.setImageUrl(dto.getImageUrl());
        course.setDemoVideo(dto.getDemoVideo());
        course.setFeatured(dto.isFeatured());
        course.setIncludesCertificate(dto.isIncludesCertificate());
        
        // ✅ FIXED: setRequirements() exists
        course.setRequirements(dto.getRequirements());
        
        course.setCreatedAt(dto.getCreatedAt());
        course.setUpdatedAt(dto.getUpdatedAt());
        course.setPublished(dto.isPublished());
        
        return course;
    }
}