package com.rnclasses.service;

import com.rnclasses.entity.Certificate;
import com.rnclasses.entity.User;
import com.rnclasses.entity.Course;
import com.rnclasses.repository.CertificateRepository;
import com.rnclasses.repository.UserRepository;
import com.rnclasses.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Value("${certificate.base-url:http://localhost:8083/api/certificate/verify/}")
    private String baseUrl;

    /**
     * Generate a unique certificate ID
     */
    private String generateCertificateId() {
        return "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Generate certificate for user after course completion
     */
    @Transactional
    public Map<String, Object> generateCertificate(Long userId, Long courseId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if certificate already exists
            if (certificateRepository.existsByUserIdAndCourseId(userId, courseId)) {
                response.put("success", false);
                response.put("message", "Certificate already exists for this course");
                return response;
            }
            
            // Get user and course
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
            
            // Generate certificate ID
            String certificateId = generateCertificateId();
            String verifyUrl = baseUrl + certificateId;
            
            // Create certificate
            Certificate certificate = new Certificate();
            certificate.setCertificateId(certificateId);
            certificate.setUserId(userId);
            certificate.setCourseId(courseId);
            certificate.setIssuedDate(LocalDateTime.now());
            certificate.setVerifyUrl(verifyUrl);
            
            Certificate savedCertificate = certificateRepository.save(certificate);
            
            // Prepare response
            response.put("success", true);
            response.put("certificateId", savedCertificate.getCertificateId());
            response.put("verifyUrl", verifyUrl);
            response.put("issuedDate", savedCertificate.getIssuedDate().toString());
            response.put("userName", user.getName());
            response.put("courseName", course.getTitle());
            response.put("instructor", course.getInstructor());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }

    /**
     * Verify certificate by ID
     */
    public Map<String, Object> verifyCertificate(String certificateId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Certificate certificate = certificateRepository.findByCertificateId(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
            
            User user = userRepository.findById(certificate.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Course course = courseRepository.findById(certificate.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            
            response.put("success", true);
            response.put("certificateId", certificate.getCertificateId());
            response.put("userName", user.getName());
            response.put("courseName", course.getTitle());
            response.put("issueDate", certificate.getIssuedDate().format(formatter));
            response.put("instructor", course.getInstructor());
            response.put("duration", course.getDuration());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }

    /**
     * Get all certificates for a user
     */
    public List<Certificate> getUserCertificates(Long userId) {
        return certificateRepository.findByUserId(userId);
    }
}