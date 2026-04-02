package com.rnclasses.service;

import com.rnclasses.entity.User;
import com.rnclasses.entity.Course;
import com.rnclasses.entity.Enrollment;
import com.rnclasses.repository.CourseRepository;
import com.rnclasses.repository.EnrollmentRepository;
import com.rnclasses.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Map<String, Object> enroll(User user, Long courseId) {
        Map<String, Object> response = new HashMap<>();
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        
        // Check if already enrolled
        if (enrollmentRepository.existsByUserAndCourse(user, course)) {
            response.put("success", false);
            response.put("message", "Already enrolled in this course");
            return response;
        }
        
        // Create new enrollment
        Enrollment enrollment = new Enrollment(user, course);
        enrollmentRepository.save(enrollment);
        
        response.put("success", true);
        response.put("message", "Successfully enrolled in course");
        response.put("enrollmentId", enrollment.getId());
        response.put("courseTitle", course.getTitle());
        
        return response;
    }

    public List<Map<String, Object>> getMyCoursesWithDetails(User user) {
        List<Enrollment> enrollments = enrollmentRepository.findByUser(user);
        
        return enrollments.stream().map(enrollment -> {
            Map<String, Object> courseData = new HashMap<>();
            Course course = enrollment.getCourse();
            
            courseData.put("id", course.getId());
            courseData.put("title", course.getTitle());
            courseData.put("description", course.getDescription());
            courseData.put("image", course.getImageUrl());
            courseData.put("progress", enrollment.getProgress());
            courseData.put("status", enrollment.getStatus());
            courseData.put("enrolledAt", enrollment.getEnrolledAt());
            courseData.put("lastAccessed", enrollment.getLastAccessed());
            
            if (course.getTrainer() != null) {
                courseData.put("instructor", course.getTrainer().getName());
            }
            
            return courseData;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getEnrollmentProgress(User user, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        
        Enrollment enrollment = enrollmentRepository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new RuntimeException("Not enrolled in this course"));
        
        Map<String, Object> progress = new HashMap<>();
        progress.put("progress", enrollment.getProgress());
        progress.put("status", enrollment.getStatus());
        progress.put("enrolledAt", enrollment.getEnrolledAt());
        progress.put("lastAccessed", enrollment.getLastAccessed());
        progress.put("completedAt", enrollment.getCompletedAt());
        
        return progress;
    }

    @Transactional
    public Map<String, Object> updateProgress(User user, Long courseId, Integer progress) {
        Map<String, Object> response = new HashMap<>();
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        
        Enrollment enrollment = enrollmentRepository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new RuntimeException("Not enrolled in this course"));
        
        enrollment.setProgress(progress);
        enrollment.setLastAccessed(LocalDateTime.now());
        
        if (progress >= 100) {
            enrollment.setStatus("COMPLETED");
            enrollment.setCompletedAt(LocalDateTime.now());
        } else {
            enrollment.setStatus("ACTIVE");
        }
        
        enrollmentRepository.save(enrollment);
        
        response.put("success", true);
        response.put("message", "Progress updated successfully");
        response.put("progress", progress);
        response.put("status", enrollment.getStatus());
        
        return response;
    }

    public Map<String, Object> getUserEnrollmentStats(User user) {
        List<Enrollment> enrollments = enrollmentRepository.findByUser(user);
        
        long total = enrollments.size();
        long completed = enrollments.stream()
                .filter(e -> "COMPLETED".equals(e.getStatus()) || e.getProgress() >= 100)
                .count();
        long active = enrollments.stream()
                .filter(e -> "ACTIVE".equals(e.getStatus()) && e.getProgress() < 100)
                .count();
        long cancelled = enrollments.stream()
                .filter(e -> "CANCELLED".equals(e.getStatus()))
                .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("completed", completed);
        stats.put("active", active);
        stats.put("cancelled", cancelled);
        stats.put("completionRate", total > 0 ? (completed * 100 / total) : 0);
        
        return stats;
    }

    @Transactional
    public Map<String, Object> cancelEnrollment(User user, Long courseId) {
        Map<String, Object> response = new HashMap<>();
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        
        Enrollment enrollment = enrollmentRepository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new RuntimeException("Not enrolled in this course"));
        
        enrollment.setStatus("CANCELLED");
        enrollmentRepository.save(enrollment);
        
        response.put("success", true);
        response.put("message", "Enrollment cancelled successfully");
        
        return response;
    }

    public boolean isEnrolled(User user, Long courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return false;
        
        return enrollmentRepository.existsByUserAndCourse(user, course);
    }
}