package com.rnclasses.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OTPService {

    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${otp.expiry-minutes:5}")
    private int otpExpiryMinutes;
    
    @Value("${otp.length:6}")
    private int otpLength;
    
    // In-memory storage for OTPs
    private final ConcurrentHashMap<String, OTPData> otpStorage = new ConcurrentHashMap<>();
    
    // Inner class to store OTP with expiry time
    private static class OTPData {
        String otp;
        long expiryTime;
        
        OTPData(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }
    
    /**
     * Generate a random OTP of specified length
     */
    public String generateOTP() {
        SecureRandom random = new SecureRandom();
        int min = (int) Math.pow(10, otpLength - 1);
        int max = (int) Math.pow(10, otpLength) - 1;
        int otp = min + random.nextInt(max - min + 1);
        return String.valueOf(otp);
    }
    
    /**
     * Save OTP in memory with expiry time
     */
    public void saveOTP(String identifier, String otp, String type) {
        String key = type + ":" + identifier;
        long expiryTime = System.currentTimeMillis() + (otpExpiryMinutes * 60 * 1000);
        otpStorage.put(key, new OTPData(otp, expiryTime));
        System.out.println("✅ OTP saved for " + identifier + ": " + otp);
    }
    
    /**
     * Verify OTP and remove it (for regular OTP verification)
     */
    public boolean verifyOTP(String identifier, String otp, String type) {
        String key = type + ":" + identifier;
        OTPData data = otpStorage.get(key);
        
        if (data == null) {
            System.out.println("❌ No OTP found for " + identifier);
            return false;
        }
        
        // Check if OTP is expired
        if (System.currentTimeMillis() > data.expiryTime) {
            otpStorage.remove(key);
            System.out.println("❌ OTP expired for " + identifier);
            return false;
        }
        
        // Verify OTP and remove it
        if (data.otp.equals(otp)) {
            otpStorage.remove(key);
            System.out.println("✅ OTP verified and removed for " + identifier);
            return true;
        }
        
        System.out.println("❌ Invalid OTP for " + identifier);
        return false;
    }
    
    /**
     * 🔥 NEW: Verify OTP WITHOUT removing it (for reset password flow)
     */
    public boolean verifyOTPForReset(String identifier, String otp, String type) {
        String key = type + ":" + identifier;
        OTPData data = otpStorage.get(key);
        
        if (data == null) {
            System.out.println("❌ No OTP found for " + identifier);
            return false;
        }
        
        // Check if OTP is expired
        if (System.currentTimeMillis() > data.expiryTime) {
            otpStorage.remove(key);
            System.out.println("❌ OTP expired for " + identifier);
            return false;
        }
        
        // Verify OTP but DON'T remove it (keep for reset password)
        if (data.otp.equals(otp)) {
            System.out.println("✅ OTP verified (kept for reset) for " + identifier);
            return true;  // OTP is NOT removed here
        }
        
        System.out.println("❌ Invalid OTP for " + identifier);
        return false;
    }
    
    /**
     * 🔥 NEW: Manually remove OTP after successful password reset
     */
    public void removeOTP(String identifier, String type) {
        String key = type + ":" + identifier;
        otpStorage.remove(key);
        System.out.println("✅ OTP removed for " + identifier);
    }
    
    /**
     * Send OTP via Email
     */
    public void sendEmailOTP(String email, String otp) {
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("Your OTP for RN Classes");
                message.setText("Your OTP is: " + otp + "\n\n" +
                        "This OTP is valid for " + otpExpiryMinutes + " minutes.\n" +
                        "If you didn't request this, please ignore this email.");
                mailSender.send(message);
                System.out.println("✅ OTP email sent to " + email);
            } catch (Exception e) {
                System.err.println("❌ Failed to send email: " + e.getMessage());
                System.out.println("📧 OTP for " + email + ": " + otp);
            }
        } else {
            System.out.println("📧 OTP for " + email + ": " + otp);
        }
    }
    
    /**
     * Send OTP via Mobile (SMS) - for future implementation
     */
    public void sendMobileOTP(String phone, String otp) {
        System.out.println("📱 OTP for " + phone + ": " + otp);
        // TODO: Integrate with SMS service like Twilio, MSG91, etc.
    }
    
    /**
     * Check if OTP exists and is valid (without verifying)
     */
    public boolean hasValidOTP(String identifier, String type) {
        String key = type + ":" + identifier;
        OTPData data = otpStorage.get(key);
        
        if (data == null) {
            return false;
        }
        
        // Check if OTP is expired
        if (System.currentTimeMillis() > data.expiryTime) {
            otpStorage.remove(key);
            return false;
        }
        
        return true;
    }
    
    /**
     * Get remaining expiry time in seconds
     */
    public long getRemainingExpiryTime(String identifier, String type) {
        String key = type + ":" + identifier;
        OTPData data = otpStorage.get(key);
        
        if (data == null) {
            return 0;
        }
        
        long remainingTime = data.expiryTime - System.currentTimeMillis();
        return remainingTime > 0 ? remainingTime / 1000 : 0;
    }
    
    /**
     * Clean up expired OTPs (can be called periodically)
     */
    public void cleanupExpiredOTPs() {
        long now = System.currentTimeMillis();
        otpStorage.entrySet().removeIf(entry -> now > entry.getValue().expiryTime);
        System.out.println("🧹 Cleaned up expired OTPs");
    }
}