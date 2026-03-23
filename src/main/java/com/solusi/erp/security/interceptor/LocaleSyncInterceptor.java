package com.solusi.erp.security.interceptor;

import com.solusi.erp.security.model.SecurityUser;
import com.solusi.erp.security.model.UserProfile;
import com.solusi.erp.security.repository.UserProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;

/**
 * Interceptor to sync the locale change (from LocaleChangeInterceptor)
 * to the UserProfile in the database if the user is authenticated.
 */
@Component
@RequiredArgsConstructor
public class LocaleSyncInterceptor implements HandlerInterceptor {

    private final UserProfileRepository userProfileRepository;

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
