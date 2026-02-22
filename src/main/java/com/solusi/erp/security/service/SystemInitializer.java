package com.solusi.erp.security.service;

import com.solusi.erp.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles initial system setup and data consistency on cold start.
 * Ensures the admin user has a valid BCrypt hash for the initial password.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SystemInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("SystemInitializer: Checking for admin user...");
        userRepository.findByUsername("admin").ifPresentOrElse(user -> {
            String currentHash = user.getPassword();
            log.info("SystemInitializer: Admin user found. Password hash starts with: {}", 
                currentHash.substring(0, Math.min(10, currentHash.length())));
            
            // Check if password is the placeholder OR the known "bad" hash from previous attempts
            boolean isPlaceholder = "INITIAL_PASSWORD_SETUP".equals(currentHash);
            boolean isOldBadHash = currentHash.startsWith("$2a$10$8.U");
            
            if (isPlaceholder || isOldBadHash) {
                log.info("Cold Start or Bad Hash detected: Synchronizing initial admin password...");
                
                String secureHash = passwordEncoder.encode("admin123");
                user.setPassword(secureHash);
                user.setPasswordChangeRequired(true); // Force change on first login
                
                userRepository.save(user);
                log.info("Admin password has been securely hashed and updated to 'admin123'.");
            } else {
                log.info("SystemInitializer: Admin password is already set and verified.");
            }
        }, () -> {
            log.warn("SystemInitializer: Admin user NOT found in database!");
        });
    }
}
