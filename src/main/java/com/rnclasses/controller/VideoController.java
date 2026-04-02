package com.rnclasses.controller;

import com.rnclasses.entity.Videoprogress;
import com.rnclasses.service.VideoProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "http://localhost:5173")
public class VideoController {

    @Autowired
    private VideoProgressService videoProgressService;

    private Long getCurrentUserId() {
        return 1L; // Placeholder - implement with your auth
    }

    @GetMapping("/progress/{courseId}")
    public ResponseEntity<?> getProgress(@PathVariable Long courseId) {
        Long userId = getCurrentUserId();
        Optional<Videoprogress> progressOpt = videoProgressService.getProgress(userId, courseId);
        
        Map<String, Object> response = new HashMap<>();
        
        if (progressOpt.isPresent()) {
            Videoprogress progress = progressOpt.get();
            response.put("userId", userId);
            response.put("courseId", courseId);
            response.put("progress", progress.getProgress());
            response.put("completed", progress.getCompleted());
            response.put("lastWatched", progress.getLastWatched());
            response.put("lastPosition", progress.getLastVideoPosition());
        } else {
            response.put("userId", userId);
            response.put("courseId", courseId);
            response.put("progress", 0);
            response.put("completed", false);
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/progress/{courseId}")
    public ResponseEntity<?> updateProgress(
            @PathVariable Long courseId,
            @RequestBody Map<String, Integer> request) {
        
        Long userId = getCurrentUserId();
        Integer watchedSeconds = request.get("watchedSeconds");
        Integer totalDuration = request.get("totalDuration");
        
        Videoprogress progress = videoProgressService.updateProgress(
            userId, courseId, watchedSeconds, totalDuration);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("progress", progress.getProgress());
        response.put("completed", progress.getCompleted());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/position/{courseId}")
    public ResponseEntity<?> updatePosition(
            @PathVariable Long courseId,
            @RequestBody Map<String, Integer> request) {
        
        Long userId = getCurrentUserId();
        Integer position = request.get("position");
        
        Videoprogress progress = videoProgressService.updatePosition(userId, courseId, position);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("position", progress.getLastVideoPosition());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete/{courseId}")
    public ResponseEntity<?> markCompleted(@PathVariable Long courseId) {
        Long userId = getCurrentUserId();
        Videoprogress progress = videoProgressService.markAsCompleted(userId, courseId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("completed", progress.getCompleted());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Long userId = getCurrentUserId();
        Map<String, Object> stats = videoProgressService.getCompletionStats(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("stats", stats);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/in-progress")
    public ResponseEntity<?> getInProgressCourses() {
        Long userId = getCurrentUserId();
        List<Videoprogress> courses = videoProgressService.getInProgressCourses(userId);
        
        List<Map<String, Object>> courseList = courses.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("courseId", p.getCourseId());
            map.put("progress", p.getProgress());
            return map;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("courses", courseList);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recently-watched")
    public ResponseEntity<?> getRecentlyWatched() {
        Long userId = getCurrentUserId();
        List<Videoprogress> courses = videoProgressService.getRecentlyWatched(userId);
        
        List<Map<String, Object>> courseList = courses.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("courseId", p.getCourseId());
            map.put("progress", p.getProgress());
            map.put("lastWatched", p.getLastWatched());
            return map;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("courses", courseList);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reset/{courseId}")
    public ResponseEntity<?> resetProgress(@PathVariable Long courseId) {
        Long userId = getCurrentUserId();
        videoProgressService.resetProgress(userId, courseId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Progress reset successfully");
        
        return ResponseEntity.ok(response);
    }
}