package com.solusi.erp.core.advice;

import com.solusi.erp.security.model.SecurityUser;
import com.solusi.erp.security.model.UserProfile;
import com.solusi.erp.security.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Controller Advice to inject global attributes like the UserProfile (Theme, etc.)
 * into all models.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {

    private final UserProfileRepository userProfileRepository;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof SecurityUser securityUser) {
            // Fetch from DB to ensure we have the latest theme/language preferences
            userProfileRepository.findByUserUsername(securityUser.getUsername())
                .ifPresent(profile -> model.addAttribute("userProfile", profile));
        }
    }
}
