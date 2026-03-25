package com.solusi.erp.core.exception;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler to catch and process exceptions across the application.
 * Supports both standard HTML views and JSON API responses.
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

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
     * Handle domain logic exceptions (DDD).
     * Automatically resolves the i18n key provided by the Domain Model.
     */
    @ExceptionHandler(DomainException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        log.warn("Domain exception: {}", ex.getKey());
        
        String localizedMessage;
        try {
            localizedMessage = messageSource.getMessage(ex.getKey(), ex.getArgs(), LocaleContextHolder.getLocale());
        } catch (org.springframework.context.NoSuchMessageException e) {
            log.warn("Missing i18n message key for DomainException: {}", ex.getKey());
            localizedMessage = ex.getKey(); // Fallback to raw key if not found
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(localizedMessage));
    }

    /**
     * Handle business logic and unexpected exceptions.
     * Cerdas: Mengembalikan JSON jika request adalah AJAX/API, atau View jika request browser biasa.
     */
    @ExceptionHandler({RuntimeException.class, Throwable.class})
    public Object handleAllExceptions(Throwable ex, HttpServletRequest request, HttpServletResponse response, Model model) {
        log.error("Exception occurred: ", ex);

        if (isAjaxRequest(request)) {
            response.resetBuffer(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ex.getMessage()));
        }

        // --- Logic Smart Redirect menggunakan Anotasi ---
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        
        if (handler instanceof HandlerMethod handlerMethod && "GET".equalsIgnoreCase(request.getMethod())) {
            DefaultRedirectUrl annotation = handlerMethod.getMethodAnnotation(DefaultRedirectUrl.class);
            if (annotation == null) {
                annotation = handlerMethod.getBeanType().getAnnotation(DefaultRedirectUrl.class);
            }

            if (annotation != null) {
                String redirectUrl = annotation.value();
                if (redirectUrl.isEmpty()) {
                    RequestMapping requestMapping = handlerMethod.getBeanType().getAnnotation(RequestMapping.class);
                    if (requestMapping != null && requestMapping.value().length > 0) {
                        redirectUrl = requestMapping.value()[0];
                    }
                }

                if (!redirectUrl.isEmpty()) {
                    log.info("Smart Redirect: Error on page {}, redirecting to: {}", request.getServletPath(), redirectUrl);
                    
                    FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
                    if (flashMap != null) {
                        flashMap.put("errorMessage", ex.getMessage());
                    }
                    
                    return "redirect:" + redirectUrl;
                }
            }
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
     * Handle Method Not Supported (405) errors.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not supported: {}", ex.getMessage());
        
        if (isAjaxRequest(request)) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .body(ApiResponse.error(ex.getMessage()));
        }
        
        return "error/404";
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
