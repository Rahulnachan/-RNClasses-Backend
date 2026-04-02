package com.rnclasses.repository;

import com.rnclasses.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // ========== BASIC METHODS ==========
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(String role);
    List<User> findByIsActiveTrue();
    List<User> findByIsActiveFalse();
    
    // ========== DATE RANGE METHODS ==========
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    // ========== SEARCH METHODS ==========
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
    
    // ========== STATISTICS METHODS ==========
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();
    
    @Query("SELECT u, SIZE(u.enrollments) as enrollmentCount FROM User u ORDER BY enrollmentCount DESC")
    List<Object[]> findTopUsersByEnrollments();
    
    // ========== ADDITIONAL HELPER METHODS ==========
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();
    
    long countByIsActiveTrue();
    
    List<User> findTop10ByOrderByCreatedAtDesc();
    
    // ========== ADMIN METHODS (Only those that don't use removed fields) ==========
    
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.email != 'nachanr99@gmail.com'")
    List<User> findOtherAdmins();
    
    // ========== COUNT METHODS FOR STATISTICS ==========
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") String role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countActiveByRole(@Param("role") String role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE DATE(u.createdAt) = CURRENT_DATE")
    long countUsersCreatedToday();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startOfWeek")
    long countUsersCreatedThisWeek(@Param("startOfWeek") LocalDateTime startOfWeek);
    
    @Query("SELECT COUNT(u) FROM User u WHERE MONTH(u.createdAt) = MONTH(CURRENT_DATE) AND YEAR(u.createdAt) = YEAR(CURRENT_DATE)")
    long countUsersCreatedThisMonth();
    
    // ========== ADDITIONAL USEFUL METHODS ==========
    
    List<User> findByRoleAndIsActiveTrue(String role);
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.coursesCreated c WHERE u.role = 'TRAINER' AND c IS NOT NULL")
    List<User> findTrainersWithCourses();
    
    @Query("UPDATE User u SET u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateLastActive(@Param("userId") Long userId);
}