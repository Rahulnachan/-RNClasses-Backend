package com.rnclasses.security;

import com.rnclasses.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtUtil jwtUtil;  // Changed from JwtUtils to JwtUtil

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Skip filter for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestURI = request.getRequestURI();
        logger.debug("Processing request: {} {}", request.getMethod(), requestURI);

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        // Extract token from header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                email = jwtUtil.extractEmail(token);
                logger.debug("Extracted email from token: {}", email);
                
                // 🔥 Extract role from token
                String role = jwtUtil.extractRole(token);
                logger.debug("Extracted role from token: {}", role);
                
            } catch (ExpiredJwtException e) {
                logger.warn("JWT token expired: {}", e.getMessage());
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
                return;
            } catch (SignatureException | MalformedJwtException e) {
                logger.warn("Invalid JWT token: {}", e.getMessage());
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            } catch (Exception e) {
                logger.error("Error processing JWT token: {}", e.getMessage());
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
                return;
            }
        }

        // Validate token and set authentication
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                if (jwtUtil.validateToken(token, userDetails)) {
                    
                    // Log user authorities
                    logger.debug("User authorities: {}", userDetails.getAuthorities());
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    logger.info("✅ Authentication successful for user: {} with roles: {}", 
                        email, 
                        userDetails.getAuthorities().stream()
                            .map(a -> a.getAuthority())
                            .collect(Collectors.joining(", "))
                    );
                } else {
                    logger.warn("❌ Token validation failed for user: {}", email);
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    return;
                }
            } catch (Exception e) {
                logger.error("Error loading user details: {}", e.getMessage());
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"%s\", \"success\": false}", message));
        response.getWriter().flush();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // List of public endpoints that don't need JWT validation
        boolean isPublic = path.startsWith("/api/auth/") || 
               path.startsWith("/api/public/") ||
               path.equals("/api/health") ||
               path.equals("/api/health/") ||
               (path.startsWith("/api/courses") && method.equals("GET")) ||
               path.equals("/api/trainer/all") ||
               path.startsWith("/api/otp/");
        
        if (isPublic) {
            logger.debug("Skipping filter for public endpoint: {}", path);
        }
        
        return isPublic;
    }
}