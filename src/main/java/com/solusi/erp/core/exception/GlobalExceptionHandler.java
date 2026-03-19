package com.solusi.erp.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("Resource not found: {}", ex.getResourcePath());
        return "error/404";
    }

    /**
     * Handle all other generic errors and exceptions (500).
     * Changed from Exception to Throwable to catch system Errors (like NoSuchMethodError).
     */
    @ExceptionHandler(Throwable.class)
    public String handleGenericError(Throwable ex, Model model) {
        log.error("Internal server error", ex);
        
        // In production, we should not show the full stack trace or internal message
        // But for development, showing the message is helpful.
        model.addAttribute("message", ex.getMessage());
        
        return "error/500";
    }
}
