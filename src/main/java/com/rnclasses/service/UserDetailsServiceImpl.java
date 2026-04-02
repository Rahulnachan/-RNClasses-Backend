package com.rnclasses.service;

import com.rnclasses.entity.User;
import com.rnclasses.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading user by email: {}", email);
        
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.warn("User not found with email: {}", email);
                        return new UsernameNotFoundException("User not found with email: " + email);
                    });

            logger.info("✅ User found: {}, role: {}, active: {}", 
                        user.getEmail(), user.getRole(), user.isActive());

            // Create authorities list with ROLE_ prefix
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
            
            logger.info("🔐 Authorities created: {}", authorities);

            return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(), // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
            );
                    
        } catch (Exception e) {
            logger.error("Error loading user: {}", e.getMessage());
            throw e;
        }
    }

    public boolean userExists(String email) {
        try {
            return userRepository.findByEmail(email).isPresent();
        } catch (Exception e) {
            logger.error("Error checking if user exists: {}", e.getMessage());
            return false;
        }
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        try {
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            logger.error("Error getting user by email: {}", e.getMessage());
            return null;
        }
    }
}