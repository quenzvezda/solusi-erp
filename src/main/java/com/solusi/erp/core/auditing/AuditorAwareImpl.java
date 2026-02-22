package com.solusi.erp.core.auditing;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

/**
 * Implementation of AuditorAware to provide the current user's identity 
 * to the JPA Auditing framework.
 * 
 * Mandate: AGENTS.md Section 4
 */
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.of("SYSTEM"); // Default auditor for system-level actions or startup
        }

        // Handle both Spring Security User object and custom string principals
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return Optional.of(((User) principal).getUsername());
        } else {
            return Optional.of(principal.toString());
        }
    }
}
