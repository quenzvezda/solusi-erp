package com.solusi.erp.core.exception;

import com.solusi.erp.core.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler to catch and process exceptions across the application.
 * Supports both standard HTML views and JSON API responses.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors for AJAX/API requests (@RequestBody).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Validation failed for API request: {}", errors);
        return ResponseEntity.badRequest().body(ApiResponse.validationError(errors));
    }

    /**
     * Handle business logic and unexpected exceptions.
     * Cerdas: Mengembalikan JSON jika request adalah AJAX/API, atau View jika request browser biasa.
     */
    @ExceptionHandler({RuntimeException.class, Throwable.class})
    public Object handleAllExceptions(Throwable ex, HttpServletRequest request, Model model) {
        log.error("Exception occurred: ", ex);

        if (isAjaxRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ex.getMessage()));
        }

        model.addAttribute("message", ex.getMessage());
        return "error/500";
    }

    /**
     * Handle Access Denied (403) errors.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        
        if (isAjaxRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied"));
        }
        
        return "error/403";
    }

    /**
     * Handle Not Found (404) errors for unmapped URLs and resources (Spring 6+).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Object handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getResourcePath());
        
        if (isAjaxRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Resource not found"));
        }
        
        return "error/404";
    }

    /**
     * Helper to detect if a request is AJAX, API, or HTMX.
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        String htmxRequest = request.getHeader("HX-Request");
        
        return "XMLHttpRequest".equals(requestedWith) || 
               "true".equals(htmxRequest) ||
               (accept != null && accept.contains("application/json"));
    }
}
