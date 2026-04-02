package com.rnclasses;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class RnClassesApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RnClassesApplication.class);
        Environment env = app.run(args).getEnvironment();
        
        // Get configuration values with defaults
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String dbUrl = env.getProperty("spring.datasource.url", "Not Configured");
        String jwtSecret = env.getProperty("jwt.secret");
        
        // Build API URL
        String apiUrl = "http://localhost:" + port + contextPath;
        
        // Print beautiful startup banner
        System.out.println("\n" +
            "╔════════════════════════════════════════════════════════════╗\n" +
            "║         RN CLASSES APPLICATION STARTED SUCCESSFULLY       ║\n" +
            "╠════════════════════════════════════════════════════════════╣\n" +
            "║  🚀 Server Port    : " + padRight(port, 35) + "║\n" +
            "║  📡 API URL        : " + padRight(apiUrl, 35) + "║\n" +
            "║  🗄️  Database      : " + padRight(getDbType(dbUrl), 35) + "║\n" +
            "║  🔐 JWT Secret     : " + padRight((jwtSecret != null ? "✅ Configured" : "❌ MISSING!"), 35) + "║\n" +
            "║  📊 Status         : " + padRight("RUNNING", 35) + "║\n" +
            "╚════════════════════════════════════════════════════════════╝\n");
        
        // Print important endpoints
        System.out.println("📌 Available Endpoints:");
        System.out.println("   ├─ Health Check : " + apiUrl + "/health");
        System.out.println("   ├─ Auth         : " + apiUrl + "/auth/login");
        System.out.println("   ├─ Courses      : " + apiUrl + "/courses");
        System.out.println("   └─ Enrollments  : " + apiUrl + "/enroll/{id}");
        System.out.println("\n✅ Application Ready! 🚀\n");
    }
    
    // Helper method to pad strings for alignment
    private static String padRight(String str, int length) {
        if (str == null) str = "null";
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < length) {
            sb.append(" ");
        }
        return sb.toString();
    }
    
    // Helper to extract database type from URL
    private static String getDbType(String dbUrl) {
        if (dbUrl.contains("mysql")) return "MySQL";
        if (dbUrl.contains("postgresql")) return "PostgreSQL";
        if (dbUrl.contains("h2")) return "H2 Database";
        if (dbUrl.contains("mongodb")) return "MongoDB";
        if (dbUrl.equals("Not Configured")) return "Not Configured";
        return "Other Database";
    }
    
    // ✅ Added Static Resource Configuration (Inner Class)
    @Configuration
    public static class StaticResourceConfig implements WebMvcConfigurer {
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Serve uploaded files from the uploads directory
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:uploads/");
            
            System.out.println("✅ Static resource handler configured: /uploads/** -> file:uploads/");
        }
    }
}