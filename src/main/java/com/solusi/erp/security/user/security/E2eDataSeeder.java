package com.solusi.erp.security.user.security;

import com.solusi.erp.security.user.infrastructure.persistence.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("e2e")
@Order(200)
public class E2eDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(E2eDataSeeder.class);

    private final UserJpaRepository userRepository;

    public E2eDataSeeder(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        log.info("E2eDataSeeder: Disabling password_change_required for all users...");
        userRepository.findByUsername("admin").ifPresent(user -> {
            user.setPasswordChangeRequired(false);
            userRepository.save(user);
            log.info("E2eDataSeeder: admin password_change_required = false");
        });
    }
}
