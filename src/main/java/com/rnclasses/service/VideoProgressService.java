package com.rnclasses.service;

import com.rnclasses.entity.Videoprogress;
import com.rnclasses.repository.VideoProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VideoProgressService {

    @Autowired
    private VideoProgressRepository videoProgressRepository;

    public Optional<Videoprogress> getProgress(Long userId, Long courseId) {
        return videoProgressRepository.findByUserIdAndCourseId(userId, courseId);
    }

    @Transactional
    public Videoprogress updateProgress(Long userId, Long courseId, Integer watchedSeconds, Integer totalDuration) {
        Videoprogress progress = videoProgressRepository.findByUserIdAndCourseId(userId, courseId)
            .orElse(new Videoprogress());
        
        progress.setUserId(userId);
        progress.setCourseId(courseId);
        progress.setWatchedDuration(watchedSeconds);
        progress.setLastWatched(LocalDateTime.now());
        
        if (totalDuration != null && totalDuration > 0) {
            int percent = (watchedSeconds * 100) / totalDuration;
            progress.setProgress(Math.min(percent, 100));
            progress.setCompleted(progress.getProgress() >= 100);
        }
        
        progress.setTotalDuration(totalDuration);
        return videoProgressRepository.save(progress);
    }

    @Transactional
    public Videoprogress updatePosition(Long userId, Long courseId, Integer position) {
        Videoprogress progress = videoProgressRepository.findByUserIdAndCourseId(userId, courseId)
            .orElse(new Videoprogress());
        
        progress.setUserId(userId);
        progress.setCourseId(courseId);
        progress.setLastVideoPosition(position);
        progress.setLastWatched(LocalDateTime.now());
        
        return videoProgressRepository.save(progress);
    }

    @Transactional
    public Videoprogress markAsCompleted(Long userId, Long courseId) {
        Videoprogress progress = videoProgressRepository.findByUserIdAndCourseId(userId, courseId)
            .orElse(new Videoprogress());
        
        progress.setUserId(userId);
        progress.setCourseId(courseId);
        progress.setCompleted(true);
        progress.setProgress(100);
        progress.setLastWatched(LocalDateTime.now());
        
        return videoProgressRepository.save(progress);
    }

    public Map<String, Object> getCompletionStats(Long userId) {
        List<Videoprogress> allProgress = videoProgressRepository.findByUserId(userId);
        
        long total = allProgress.size();
        long completed = allProgress.stream().filter(Videoprogress::getCompleted).count();
        long inProgress = allProgress.stream()
            .filter(p -> !p.getCompleted() && p.getProgress() > 0)
            .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCourses", total);
        stats.put("completedCourses", completed);
        stats.put("inProgressCourses", inProgress);
        
        return stats;
    }

    public List<Videoprogress> getInProgressCourses(Long userId) {
        return videoProgressRepository.findInProgressCourses(userId);
    }

    public List<Videoprogress> getRecentlyWatched(Long userId) {
        return videoProgressRepository.findRecentlyWatched(userId);
    }

    @Transactional
    public void resetProgress(Long userId, Long courseId) {
        videoProgressRepository.findByUserIdAndCourseId(userId, courseId)
            .ifPresent(progress -> {
                progress.setProgress(0);
                progress.setCompleted(false);
                progress.setWatchedDuration(0);
                progress.setLastVideoPosition(0);
                progress.setLastWatched(LocalDateTime.now());
                videoProgressRepository.save(progress);
            });
    }

    public boolean isCourseCompleted(Long userId, Long courseId) {
        return videoProgressRepository.findByUserIdAndCourseId(userId, courseId)
            .map(Videoprogress::getCompleted)
            .orElse(false);
    }
}