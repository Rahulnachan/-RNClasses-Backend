package com.rnclasses.repository;

import com.rnclasses.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    // ==================== FILTERING METHODS ====================
    
    // Find by category (for category filter)
    List<Course> findByCategory(String category);
    
    // Find by level (Beginner, Intermediate, Advanced)
    List<Course> findByLevel(String level);
    
    // Find by category and level (combined filter)
    List<Course> findByCategoryAndLevel(String category, String level);
    
    // Find featured courses
    List<Course> findByFeaturedTrue();
    
    // Find published courses
    List<Course> findByPublishedTrue();
    
    // Find draft courses
    List<Course> findByPublishedFalse();
    
    // ==================== NEW APPROVAL METHODS ====================
    
    // Find courses by approval status
    List<Course> findByApprovalStatus(String approvalStatus);
    
    // Find pending courses (for admin approval)
    List<Course> findByApprovalStatusOrderByCreatedAtDesc(String approvalStatus);
    
    // Find courses by trainer and approval status
    List<Course> findByTrainerIdAndApprovalStatus(Long trainerId, String approvalStatus);
    
    // Find courses by trainer with their approval status
    @Query("SELECT c FROM Course c WHERE c.trainer.id = :trainerId ORDER BY c.approvalStatus, c.createdAt DESC")
    List<Course> findByTrainerIdWithApprovalStatus(@Param("trainerId") Long trainerId);
    
    // Count pending courses
    @Query("SELECT COUNT(c) FROM Course c WHERE c.approvalStatus = 'PENDING'")
    long countPendingCourses();
    
    // Count approved courses
    @Query("SELECT COUNT(c) FROM Course c WHERE c.approvalStatus = 'APPROVED'")
    long countApprovedCourses();
    
    // Count rejected courses
    @Query("SELECT COUNT(c) FROM Course c WHERE c.approvalStatus = 'REJECTED'")
    long countRejectedCourses();
    
    // Count pending courses by trainer
    @Query("SELECT COUNT(c) FROM Course c WHERE c.trainer.id = :trainerId AND c.approvalStatus = 'PENDING'")
    long countPendingCoursesByTrainerId(@Param("trainerId") Long trainerId);
    
    // Find all courses with approval status (for admin)
    @Query("SELECT c FROM Course c ORDER BY c.approvalStatus, c.createdAt DESC")
    List<Course> findAllWithApprovalStatus();
    
    // Find rejected courses with reason
    @Query("SELECT c FROM Course c WHERE c.approvalStatus = 'REJECTED' AND c.rejectionReason IS NOT NULL")
    List<Course> findRejectedCoursesWithReason();
    
    // ==================== SEARCH METHODS ====================
    
    // Search by title
    @Query("SELECT c FROM Course c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Course> searchByTitle(@Param("searchTerm") String searchTerm);
    
    // Search by title or description
    @Query("SELECT c FROM Course c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Course> searchByTitleOrDescription(@Param("searchTerm") String searchTerm);
    
    // Search only approved courses
    @Query("SELECT c FROM Course c WHERE (LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND c.approvalStatus = 'APPROVED'")
    List<Course> searchApprovedCourses(@Param("searchTerm") String searchTerm);
    
    // ==================== SORTING METHODS ====================
    
    // Find top rated courses (only approved)
    @Query("SELECT c FROM Course c WHERE c.approvalStatus = 'APPROVED' ORDER BY c.rating DESC")
    List<Course> findTopRatedApprovedCourses();
    
    List<Course> findTop10ByOrderByRatingDesc();
    List<Course> findTop10ByOrderByStudentsCountDesc();
    List<Course> findTop10ByOrderByCreatedAtDesc();
    
    // ==================== INSTRUCTOR METHODS ====================
    
    List<Course> findByTrainerId(Long trainerId);
    List<Course> findByTrainerIdAndPublishedTrue(Long trainerId);
    List<Course> findByTrainerIdAndPublishedFalse(Long trainerId);
    
    @Query("SELECT c FROM Course c WHERE c.trainer.name LIKE %:trainerName%")
    List<Course> findByTrainerName(@Param("trainerName") String trainerName);
    
    // Get only approved courses by trainer
    @Query("SELECT c FROM Course c WHERE c.trainer.id = :trainerId AND c.approvalStatus = 'APPROVED'")
    List<Course> findApprovedCoursesByTrainerId(@Param("trainerId") Long trainerId);
    
    // ==================== STATISTICS METHODS ====================
    
    @Query("SELECT c.category, COUNT(c) FROM Course c GROUP BY c.category")
    List<Object[]> countCoursesByCategory();
    
    @Query("SELECT c.level, COUNT(c) FROM Course c GROUP BY c.level")
    List<Object[]> countCoursesByLevel();
    
    @Query("SELECT COUNT(c) FROM Course c WHERE c.published = true")
    long countByPublishedTrue();
    
    @Query("SELECT COUNT(c) FROM Course c WHERE c.published = false")
    long countByPublishedFalse();
    
    @Query("SELECT COUNT(c) FROM Course c WHERE c.trainer.id = :trainerId")
    long countByTrainerId(@Param("trainerId") Long trainerId);
    
    @Query("SELECT COUNT(c) FROM Course c WHERE c.trainer.id = :trainerId AND c.published = true")
    long countPublishedByTrainerId(@Param("trainerId") Long trainerId);
    
    @Query("SELECT c.category, AVG(c.rating) FROM Course c GROUP BY c.category")
    List<Object[]> getAverageRatingByCategory();
    
    @Query("SELECT AVG(c.rating) FROM Course c WHERE c.trainer.id = :trainerId")
    Double getAverageRatingByTrainerId(@Param("trainerId") Long trainerId);
    
    // Approval statistics
    @Query("SELECT c.approvalStatus, COUNT(c) FROM Course c GROUP BY c.approvalStatus")
    List<Object[]> countCoursesByApprovalStatus();
    
    @Query("SELECT c.trainer.id, c.trainer.name, COUNT(c) FROM Course c WHERE c.approvalStatus = 'PENDING' GROUP BY c.trainer.id, c.trainer.name")
    List<Object[]> countPendingCoursesByTrainer();
    
    // ==================== PRICE RANGE METHODS ====================
    
    List<Course> findByPriceBetween(Double minPrice, Double maxPrice);
    
    @Query("SELECT c FROM Course c WHERE (c.price IS NULL OR c.price = 0) AND c.approvalStatus = 'APPROVED'")
    List<Course> findFreeApprovedCourses();
    
    @Query("SELECT c FROM Course c WHERE c.price IS NULL OR c.price = 0")
    List<Course> findFreeCourses();
    
    // ==================== ADVANCED FILTERING ====================
    
    @Query("SELECT c FROM Course c WHERE " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:level IS NULL OR c.level = :level) AND " +
           "(:minPrice IS NULL OR c.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR c.price <= :maxPrice) AND " +
           "(:published IS NULL OR c.published = :published) AND " +
           "(:approvalStatus IS NULL OR c.approvalStatus = :approvalStatus) AND " +
           "(:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Course> filterCourses(
            @Param("category") String category,
            @Param("level") String level,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("published") Boolean published,
            @Param("approvalStatus") String approvalStatus,
            @Param("search") String search);
    
    // ==================== ENROLLMENT RELATED ====================
    
    @Query("SELECT c, SIZE(c.enrollments) as enrollmentCount FROM Course c WHERE c.approvalStatus = 'APPROVED' ORDER BY enrollmentCount DESC")
    List<Object[]> findTopApprovedCoursesByEnrollments();
    
    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.user.id = :userId")
    List<Course> findCoursesByUserId(@Param("userId") Long userId);
    
    // ==================== COURSE STATUS ====================
    
    @Query("SELECT COUNT(c) FROM Course c")
    long countTotalCourses();
    
    @Query("SELECT SUM(c.studentsCount) FROM Course c")
    Long getTotalStudentsEnrolled();
    
    // ==================== DASHBOARD STATISTICS ====================
    
    @Query("SELECT " +
           "COUNT(c) as totalCourses, " +
           "SUM(CASE WHEN c.published = true THEN 1 ELSE 0 END) as publishedCourses, " +
           "SUM(CASE WHEN c.published = false THEN 1 ELSE 0 END) as draftCourses, " +
           "SUM(CASE WHEN c.approvalStatus = 'PENDING' THEN 1 ELSE 0 END) as pendingCourses, " +
           "SUM(CASE WHEN c.approvalStatus = 'APPROVED' THEN 1 ELSE 0 END) as approvedCourses, " +
           "SUM(CASE WHEN c.approvalStatus = 'REJECTED' THEN 1 ELSE 0 END) as rejectedCourses, " +
           "AVG(c.rating) as avgRating, " +
           "SUM(c.studentsCount) as totalStudents " +
           "FROM Course c")
    List<Object[]> getCompleteCourseStatistics();
    
    @Query("SELECT c.category, COUNT(c), AVG(c.rating), SUM(c.studentsCount) FROM Course c WHERE c.approvalStatus = 'APPROVED' GROUP BY c.category")
    List<Object[]> getApprovedCategoryWiseStats();
    
    // ==================== DATE BASED METHODS ====================
    
    @Query("SELECT c FROM Course c WHERE c.createdAt >= :date")
    List<Course> findCoursesCreatedAfter(@Param("date") java.time.LocalDateTime date);
    
    @Query("SELECT COUNT(c) FROM Course c WHERE c.createdAt >= :startDate")
    long countCoursesCreatedSince(@Param("startDate") java.time.LocalDateTime startDate);
    
    @Query("SELECT c FROM Course c WHERE c.approvedAt BETWEEN :startDate AND :endDate")
    List<Course> findCoursesApprovedBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                            @Param("endDate") java.time.LocalDateTime endDate);
}