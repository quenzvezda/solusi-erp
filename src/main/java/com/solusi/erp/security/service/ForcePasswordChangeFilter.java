package com.solusi.erp.security.service;

import com.solusi.erp.security.model.SecurityUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to intercept requests and redirect users to the password reset page
 * if the 'password_change_required' flag is set to true.
 */
@Component
public class ForcePasswordChangeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requestUri = request.getRequestURI();

        // Skip filter for static resources, login, and the reset password page itself
        if (isPublicPath(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof SecurityUser securityUser) {
                if (securityUser.user().isPasswordChangeRequired() && !requestUri.equals("/reset-password")) {
                    response.sendRedirect("/reset-password");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String uri) {
        return uri.startsWith("/libs/") || 
               uri.startsWith("/static/") || 
               uri.startsWith("/css/") ||
               uri.startsWith("/js/") ||
               uri.startsWith("/img/") ||
               uri.startsWith("/login") || 
               uri.startsWith("/logout") ||
               uri.startsWith("/reset-password") ||
               uri.equals("/favicon.ico") ||
               uri.equals("/error");
    }
}
