package com.solusi.erp.security.user.security;

import com.solusi.erp.security.user.infrastructure.persistence.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class SystemInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SystemInitializer.class);

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SystemInitializer(UserJpaRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("SystemInitializer: Checking for admin user...");
        userRepository.findByUsername("admin").ifPresentOrElse(user -> {
            String currentHash = user.getPassword();
            boolean isPlaceholder = "INITIAL_PASSWORD_SETUP".equals(currentHash);
            boolean isOldBadHash = currentHash.startsWith("$2a$10$8.U");
            if (isPlaceholder || isOldBadHash) {
                user.setPassword(passwordEncoder.encode("admin123"));
                user.setPasswordChangeRequired(true);
                userRepository.save(user);
                log.info("Admin password synchronized.");
            }
        }, () -> log.warn("SystemInitializer: Admin user NOT found in database!"));
    }
}
