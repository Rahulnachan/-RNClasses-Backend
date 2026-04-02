package com.rnclasses.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@rnclasses.com}")
    private String fromEmail;

    public void sendWelcomeEmail(String email, String name) {
        System.out.println("📧 Sending welcome email to: " + email);
        System.out.println("Welcome " + name + " to RN Classes!");
        
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject("Welcome to RN Classes! 🎉");
                message.setText("Dear " + name + ",\n\n" +
                        "Welcome to RN Classes! We're excited to have you on board.\n\n" +
                        "Start your learning journey today by exploring our courses.\n\n" +
                        "Best regards,\n" +
                        "The RN Classes Team");
                mailSender.send(message);
                System.out.println("✅ Welcome email sent to " + email);
            } catch (Exception e) {
                System.err.println("❌ Failed to send email: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Mail sender not configured. Email would be sent to: " + email);
        }
    }

    public void sendEnrollmentEmail(String email, String course) {
        System.out.println("📧 Sending enrollment email to: " + email);
        System.out.println("Enrolled in course: " + course);
        
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject("Course Enrollment Confirmation ✅");
                message.setText("Dear Student,\n\n" +
                        "You have successfully enrolled in the course: " + course + "\n\n" +
                        "Start learning now and complete the course to earn your certificate!\n\n" +
                        "Best regards,\n" +
                        "The RN Classes Team");
                mailSender.send(message);
                System.out.println("✅ Enrollment email sent to " + email);
            } catch (Exception e) {
                System.err.println("❌ Failed to send email: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Mail sender not configured. Email would be sent to: " + email);
        }
    }

    public void sendPaymentReceipt(String email, Map<String, Object> paymentDetails) {
        System.out.println("📧 Sending payment receipt to: " + email);
        System.out.println("Payment details: " + paymentDetails);
        
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject("Payment Receipt - RN Classes 💰");
                
                StringBuilder sb = new StringBuilder();
                sb.append("Dear Student,\n\n");
                sb.append("Thank you for your payment. Here are the details:\n\n");
                
                for (Map.Entry<String, Object> entry : paymentDetails.entrySet()) {
                    sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
                
                sb.append("\nYou can now access your course.\n\n");
                sb.append("Best regards,\nThe RN Classes Team");
                
                message.setText(sb.toString());
                mailSender.send(message);
                System.out.println("✅ Payment receipt sent to " + email);
            } catch (Exception e) {
                System.err.println("❌ Failed to send email: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Mail sender not configured. Payment receipt would be sent to: " + email);
        }
    }

    public void subscribeToNewsletter(String email) {
        System.out.println("📧 Subscribing to newsletter: " + email);
        
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject("Welcome to RN Classes Newsletter! 📰");
                message.setText("Dear Subscriber,\n\n" +
                        "Thank you for subscribing to our newsletter!\n\n" +
                        "You'll now receive updates about new courses, special offers, and learning resources.\n\n" +
                        "Best regards,\n" +
                        "The RN Classes Team");
                mailSender.send(message);
                System.out.println("✅ Newsletter subscription confirmed for " + email);
            } catch (Exception e) {
                System.err.println("❌ Failed to send newsletter confirmation: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Mail sender not configured. Newsletter subscription for: " + email);
        }
    }
}