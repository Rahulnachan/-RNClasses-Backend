package com.rnclasses.repository;

import com.rnclasses.entity.Enrollment;
import com.rnclasses.entity.User;
import com.rnclasses.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // ===== EXISTING METHODS (using Entity objects) =====
    
    // Check if user is already enrolled in a course (using Entity objects)
    boolean existsByUserAndCourse(User user, Course course);

    // Get all enrollments of a user (using User object)
    List<Enrollment> findByUser(User user);
    
    // Get specific enrollment (using Entity objects)
    Optional<Enrollment> findByUserAndCourse(User user, Course course);
    
    // Get all enrollments for a course (using Course object)
    List<Enrollment> findByCourse(Course course);
    
    // Count how many students enrolled in a course (using Course object)
    Long countByCourse(Course course);
    
    // Get active enrollments for a user (using User object)
    @Query("SELECT e FROM Enrollment e WHERE e.user = :user AND e.status = 'ACTIVE'")
    List<Enrollment> findActiveEnrollmentsByUser(@Param("user") User user);
    
    // Get completed enrollments for a user (using User object)
    @Query("SELECT e FROM Enrollment e WHERE e.user = :user AND e.status = 'COMPLETED'")
    List<Enrollment> findCompletedEnrollmentsByUser(@Param("user") User user);
    
    // Get enrollments ordered by date (using User object)
    List<Enrollment> findByUserOrderByEnrolledAtDesc(User user);
    
    // ===== METHODS USING IDs =====
    
    // Check if user is already enrolled using IDs
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e WHERE e.user.id = :userId AND e.course.id = :courseId")
    boolean existsByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
    
    // Get enrollment by user ID and course ID
    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId AND e.course.id = :courseId")
    Optional<Enrollment> findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
    
    // Get all enrollments by user ID
    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId")
    List<Enrollment> findByUserId(@Param("userId") Long userId);
    
    // Get all enrollments by course ID
    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId")
    List<Enrollment> findByCourseId(@Param("courseId") Long courseId);
    
    // Get active enrollments by user ID
    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId AND e.status = 'ACTIVE'")
    List<Enrollment> findActiveEnrollmentsByUserId(@Param("userId") Long userId);
    
    // Get completed enrollments by user ID
    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId AND e.status = 'COMPLETED'")
    List<Enrollment> findCompletedEnrollmentsByUserId(@Param("userId") Long userId);
    
    // Count enrollments by course ID
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);
    
    // Count enrollments by user ID
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    // Count enrollments by status
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.status = :status")
    Long countByStatus(@Param("status") String status);
    
    // Count active enrollments for a course
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'ACTIVE'")
    Long countActiveByCourseId(@Param("courseId") Long courseId);
    
    // Count completed enrollments for a course
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'COMPLETED'")
    Long countCompletedByCourseId(@Param("courseId") Long courseId);
    
    // ===== TRAINER SPECIFIC METHODS =====
    
    // Get all enrollments for courses taught by a specific trainer
    @Query("SELECT e FROM Enrollment e WHERE e.course.trainer.id = :trainerId")
    List<Enrollment> findByTrainerId(@Param("trainerId") Long trainerId);
    
    // Get enrollments by trainer with status filter
    @Query("SELECT e FROM Enrollment e WHERE e.course.trainer.id = :trainerId AND e.status = :status")
    List<Enrollment> findByTrainerIdAndStatus(@Param("trainerId") Long trainerId, @Param("status") String status);
    
    // Count enrollments for a trainer's courses
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.trainer.id = :trainerId")
    Long countByTrainerId(@Param("trainerId") Long trainerId);
    
    // Get recent enrollments for a trainer's courses
    List<Enrollment> findByCourseTrainerIdOrderByEnrolledAtDesc(Long trainerId, org.springframework.data.domain.Pageable pageable);
    
    @Query(value = "SELECT * FROM enrollments e JOIN courses c ON e.course_id = c.id WHERE c.trainer_id = :trainerId ORDER BY e.enrolled_at DESC LIMIT :limit", nativeQuery = true)
    List<Enrollment> findRecentByTrainerIdNative(@Param("trainerId") Long trainerId, @Param("limit") int limit);
    
    // ===== NEW METHODS FOR APPROVED COURSES ONLY =====
    
    // Get enrollments for approved courses only
    @Query("SELECT e FROM Enrollment e WHERE e.course.approvalStatus = 'APPROVED'")
    List<Enrollment> findByApprovedCourses();
    
    // Get enrollments by user for approved courses only
    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId AND e.course.approvalStatus = 'APPROVED'")
    List<Enrollment> findByUserIdAndApprovedCourse(@Param("userId") Long userId);
    
    // Count enrollments for approved courses
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.approvalStatus = 'APPROVED'")
    Long countByApprovedCourses();
    
    // Get total revenue from approved courses only
    @Query("SELECT SUM(c.price) FROM Enrollment e JOIN e.course c WHERE e.course.approvalStatus = 'APPROVED' AND e.status IN ('ACTIVE', 'COMPLETED')")
    Double getTotalRevenueFromApprovedCourses();
    
    // ===== STATISTICS AND DASHBOARD METHODS =====
    
    // Get recent enrollments (for admin dashboard)
    List<Enrollment> findTop10ByOrderByEnrolledAtDesc();
    
    List<Enrollment> findAllByOrderByEnrolledAtDesc(org.springframework.data.domain.Pageable pageable);
    
    // Get enrollment statistics by status
    @Query("SELECT e.status, COUNT(e) FROM Enrollment e GROUP BY e.status")
    List<Object[]> countEnrollmentsByStatus();
    
    // Get enrollment statistics by course (only approved courses)
    @Query("SELECT c.title, COUNT(e) FROM Enrollment e JOIN e.course c WHERE c.approvalStatus = 'APPROVED' GROUP BY c.id ORDER BY COUNT(e) DESC")
    List<Object[]> countEnrollmentsByApprovedCourse();
    
    // Get enrollment statistics by month
    @Query("SELECT FUNCTION('MONTH', e.enrolledAt), FUNCTION('YEAR', e.enrolledAt), COUNT(e) FROM Enrollment e GROUP BY FUNCTION('YEAR', e.enrolledAt), FUNCTION('MONTH', e.enrolledAt) ORDER BY FUNCTION('YEAR', e.enrolledAt) DESC, FUNCTION('MONTH', e.enrolledAt) DESC")
    List<Object[]> getEnrollmentsByMonth();
    
    // Count enrollments created in a date range
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.enrolledAt BETWEEN :startDate AND :endDate")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // ===== REVENUE METHODS =====
    
    // Get total revenue (sum of course prices for completed/active enrollments)
    @Query("SELECT SUM(c.price) FROM Enrollment e JOIN e.course c WHERE e.status IN ('ACTIVE', 'COMPLETED')")
    Double getTotalRevenue();
    
    // Get revenue by trainer
    @Query("SELECT SUM(c.price) FROM Enrollment e JOIN e.course c WHERE c.trainer.id = :trainerId AND e.status IN ('ACTIVE', 'COMPLETED')")
    Double getRevenueByTrainerId(@Param("trainerId") Long trainerId);
    
    // Get revenue by date range
    @Query("SELECT SUM(c.price) FROM Enrollment e JOIN e.course c WHERE e.enrolledAt BETWEEN :startDate AND :endDate AND e.status IN ('ACTIVE', 'COMPLETED')")
    Double getRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Get revenue by course
    @Query("SELECT SUM(c.price) FROM Enrollment e JOIN e.course c WHERE c.id = :courseId AND e.status IN ('ACTIVE', 'COMPLETED')")
    Double getRevenueByCourseId(@Param("courseId") Long courseId);
    
    // ===== PROGRESS RELATED METHODS =====
    
    // Get enrollments with progress less than 100 (in progress)
    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId AND e.progress < 100 AND e.status = 'ACTIVE'")
    List<Enrollment> findInProgressByUserId(@Param("userId") Long userId);
    
    // Get average progress for a course
    @Query("SELECT AVG(e.progress) FROM Enrollment e WHERE e.course.id = :courseId")
    Double getAverageProgressByCourseId(@Param("courseId") Long courseId);
    
    // Get students who haven't accessed the course in a while
    @Query("SELECT e FROM Enrollment e WHERE e.lastAccessed < :date AND e.status = 'ACTIVE'")
    List<Enrollment> findStaleEnrollments(@Param("date") LocalDateTime date);
    
    // Get completion rate for a course
    @Query("SELECT COUNT(e) * 1.0 / (SELECT COUNT(*) FROM Enrollment e2 WHERE e2.course.id = :courseId) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'COMPLETED'")
    Double getCourseCompletionRate(@Param("courseId") Long courseId);
    
    // ===== BULK OPERATIONS =====
    
    // Delete all enrollments for a course
    @Query("DELETE FROM Enrollment e WHERE e.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);
    
    // Delete all enrollments for a user
    @Query("DELETE FROM Enrollment e WHERE e.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    // ===== ENROLLMENT TRENDS =====
    
    // Get daily enrollment count for the last 30 days
    @Query(value = "SELECT DATE(enrolled_at) as date, COUNT(*) as count FROM enrollments WHERE enrolled_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) GROUP BY DATE(enrolled_at) ORDER BY date", nativeQuery = true)
    List<Object[]> getDailyEnrollmentTrends();
    
    // Get popular courses by enrollment count
    @Query("SELECT c.title, COUNT(e) FROM Enrollment e JOIN e.course c WHERE c.approvalStatus = 'APPROVED' GROUP BY c.id ORDER BY COUNT(e) DESC")
    List<Object[]> getPopularCourses();
}
