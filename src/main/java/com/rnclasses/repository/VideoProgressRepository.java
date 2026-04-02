package com.rnclasses.repository;

import com.rnclasses.entity.Videoprogress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoProgressRepository extends JpaRepository<Videoprogress, Long> {

    Optional<Videoprogress> findByUserIdAndCourseId(Long userId, Long courseId);

    List<Videoprogress> findByUserId(Long userId);

    @Query("SELECT v FROM Videoprogress v WHERE v.userId = :userId AND v.completed = false AND v.progress > 0")
    List<Videoprogress> findInProgressCourses(@Param("userId") Long userId);

    @Query("SELECT v FROM Videoprogress v WHERE v.userId = :userId ORDER BY v.lastWatched DESC")
    List<Videoprogress> findRecentlyWatched(@Param("userId") Long userId);
}