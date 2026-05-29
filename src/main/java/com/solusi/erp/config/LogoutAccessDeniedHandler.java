package com.solusi.erp.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LogoutAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (isLogoutRequest(request) && isAnonymousAuthentication()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    private boolean isLogoutRequest(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestPath = request.getRequestURI();
        if (!contextPath.isBlank() && requestPath.startsWith(contextPath)) {
            requestPath = requestPath.substring(contextPath.length());
        }
        return "POST".equalsIgnoreCase(request.getMethod()) && "/logout".equals(requestPath);
    }

    private boolean isAnonymousAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }
}
