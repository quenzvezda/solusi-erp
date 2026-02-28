package com.solusi.erp.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global Exception Handler to catch and process exceptions across the application.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle Access Denied (403) errors.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return "error/403";
    }

    /**
     * Handle Not Found (404) errors for unmapped URLs and resources (Spring 6+).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("Resource not found: {}", ex.getResourcePath());
        return "error/404";
    }

    /**
     * Handle all other generic exceptions (500).
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Internal server error", ex);
        
        // In production, we should not show the full stack trace or internal message
        // But for development, showing the message is helpful.
        model.addAttribute("message", ex.getMessage());
        
        return "error/500";
    }
}
