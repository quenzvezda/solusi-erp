package com.solusi.erp.security.user.security;

import com.solusi.erp.security.shared.model.SecurityUser;
import com.solusi.erp.security.user.infrastructure.persistence.UserProfile;
import com.solusi.erp.security.user.infrastructure.persistence.UserProfileJpaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LocaleSyncInterceptor implements HandlerInterceptor {

    private final UserProfileJpaRepository userProfileRepository;

    public LocaleSyncInterceptor(UserProfileJpaRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String langParam = request.getParameter("lang");
        if (langParam != null && SecurityContextHolder.getContext().getAuthentication() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof SecurityUser securityUser) {
                UserProfile profile = securityUser.user().getProfile();
                if (profile != null && !langParam.equals(profile.getLanguageCode())) {
                    profile.setLanguageCode(langParam);
                    userProfileRepository.save(profile);
                }
            }
        }
    }
}
