package com.solusi.erp.core.auditing;

import com.solusi.erp.security.shared.model.SecurityUser;
import com.solusi.erp.security.user.infrastructure.persistence.UserJpaRepository;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Implementation of AuditorAware to provide the current user's ID
 * to the JPA Auditing framework.
 *
 * Returns Long (user ID) instead of String to support FK-based auditing.
 * Falls back to ID 1 (admin) for unauthenticated / system-level operations.
 *
 * Mandate: AGENTS.md Section 4
 */
public class AuditorAwareImpl implements AuditorAware<Long> {

    private static final Long SYSTEM_USER_ID = 1L;

    private final UserJpaRepository userRepository;

    public AuditorAwareImpl(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of(SYSTEM_USER_ID);
        }

        Object principal = authentication.getPrincipal();

        // Optimized: Get ID directly from SecurityUser to avoid DB query and potential infinite flush loops
        if (principal instanceof SecurityUser securityUser) {
            return Optional.ofNullable(securityUser.user().getId());
        }

        // Final fallback for anonymous or non-standard principals (e.g. testing)
        // We avoid calling userRepository.findByUsername here to prevent infinite recursion during flushes.
        return Optional.of(SYSTEM_USER_ID);
    }
}
