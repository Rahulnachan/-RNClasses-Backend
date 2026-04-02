package com.rnclasses.controller;

import com.rnclasses.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/certificate")
@CrossOrigin(origins = "http://localhost:5173")
public class Certificatecontroller {

    @Autowired
    private CertificateService certificateService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateCertificate(@RequestBody Map<String, Long> request) {
        Long courseId = request.get("courseId");
        Long userId = request.get("userId");
        
        Map<String, Object> certificate = certificateService.generateCertificate(userId, courseId);
        return ResponseEntity.ok(certificate);
    }

    @GetMapping("/verify/{certificateId}")
    public ResponseEntity<?> verifyCertificate(@PathVariable String certificateId) {
        Map<String, Object> certificate = certificateService.verifyCertificate(certificateId);
        
        if (certificate != null) {
            return ResponseEntity.ok(Map.of("success", true, "data", certificate));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid certificate"));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserCertificates(@PathVariable Long userId) {
        return ResponseEntity.ok(certificateService.getUserCertificates(userId));
    }
}