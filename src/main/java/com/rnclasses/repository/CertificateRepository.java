package com.rnclasses.repository;

import com.rnclasses.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    
    // Find certificate by certificate ID (for verification)
    Optional<Certificate> findByCertificateId(String certificateId);
    
    // Find all certificates for a user
    List<Certificate> findByUserId(Long userId);
    
    // Find certificate for specific user and course
    Optional<Certificate> findByUserIdAndCourseId(Long userId, Long courseId);
    
    // Check if certificate exists for user and course
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}