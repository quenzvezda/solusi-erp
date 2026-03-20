package com.solusi.erp.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Utility for standardizing HTMX responses in controllers.
 * Provides both String-based (legacy) and ResponseEntity-based (new) methods.
 */
public class HtmxResponseUtility {

    private static final String HX_REDIRECT = "HX-Redirect";
    private static final String GLOBAL_ERROR_FRAGMENT = "fragments/alerts :: error";

    // --- LEGACY METHODS (Return String) ---

    public static String redirect(HttpServletResponse response, String url) {
        response.setHeader(HX_REDIRECT, url);
        return null;
    }

    public static String returnErrorFragment() {
        return GLOBAL_ERROR_FRAGMENT;
    }

    public static String handleException(Model model, String message) {
        model.addAttribute("errorMessage", message);
        return GLOBAL_ERROR_FRAGMENT;
    }

    // --- NEW EXPERIMENTAL METHODS (Return ResponseEntity) ---

    public static ResponseEntity<String> redirectResponse(HttpServletResponse response, String url) {
        response.setHeader(HX_REDIRECT, url);
        return ResponseEntity.ok().build();
    }

    public static ResponseEntity<String> redirectResponse(HttpServletResponse response, String url, String message, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("successMessage", message);
        response.setHeader(HX_REDIRECT, url);
        return ResponseEntity.ok().build();
    }

    public static ResponseEntity<String> handleExceptionResponse(Model model, String message) {
        model.addAttribute("errorMessage", message);
        return ResponseEntity.ok().header("HX-Trigger", "errorOccurred").body(GLOBAL_ERROR_FRAGMENT);
    }

    public static ResponseEntity<String> renderErrorResponse(Model model, String template, String fragment) {
        return ResponseEntity.ok().header("HX-Trigger", "validationError").body(template + " :: " + fragment);
    }
}
