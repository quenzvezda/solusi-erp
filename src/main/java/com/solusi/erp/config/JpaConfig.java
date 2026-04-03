package com.solusi.erp.config;

import com.solusi.erp.core.auditing.AuditorAwareImpl;
import com.solusi.erp.security.user.infrastructure.persistence.UserJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration to enable JPA Auditing and register AuditorAware bean.
 * AuditorAware<Long> returns the current user's ID for FK-based auditing.
 *
 * Mandate: AGENTS.md Section 4
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {

    @Bean
    public AuditorAware<Long> auditorAware(UserJpaRepository userRepository) {
        return new AuditorAwareImpl(userRepository);
    }
}
