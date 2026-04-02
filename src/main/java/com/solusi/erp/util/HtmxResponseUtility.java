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
    private static final String HX_TRIGGER = "HX-Trigger";
    
    // Standard Events
    private static final String EVENT_REFRESH_TABLE = "refresh-table";
    private static final String EVENT_SHOW_SUCCESS = "erp:show-success";
    private static final String EVENT_SHOW_WARNING = "erp:show-warning";
    
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
        return ResponseEntity.ok().header(HX_TRIGGER, "errorOccurred").body(GLOBAL_ERROR_FRAGMENT);
    }

    public static ResponseEntity<String> renderErrorResponse(Model model, String template, String fragment) {
        return ResponseEntity.ok().header(HX_TRIGGER, "validationError").body(template + " :: " + fragment);
    }

    // --- TRIGGER HELPERS (Standardizing Success/Refresh responses) ---

    /**
     * Returns an empty OK response with refresh-table trigger.
     */
    public static ResponseEntity<Void> okWithRefreshTable() {
        return ResponseEntity.ok().header(HX_TRIGGER, EVENT_REFRESH_TABLE).build();
    }

    /**
     * Returns an empty OK response with a success message trigger.
     */
    public static ResponseEntity<Void> okWithSuccess(String message) {
        String json = String.format("{\"%s\":\"%s\"}", EVENT_SHOW_SUCCESS, escapeJson(message));
        return ResponseEntity.ok().header(HX_TRIGGER, json).build();
    }

    /**
     * Returns an empty OK response with both refresh-table and success message trigger.
     * Perfect for Delete or Toggle Status operations.
     */
    public static ResponseEntity<Void> okWithRefreshTableAndSuccess(String message) {
        String json = String.format("{\"%s\":true, \"%s\":\"%s\"}", EVENT_REFRESH_TABLE, EVENT_SHOW_SUCCESS, escapeJson(message));
        return ResponseEntity.ok().header(HX_TRIGGER, json).build();
    }

    /**
     * Returns an empty OK response with both refresh-table and warning message trigger.
     * Used by Smart Delete when entity is in use and soft-deleted instead of hard-deleted.
     */
    public static ResponseEntity<Void> okWithRefreshTableAndWarning(String message) {
        String json = String.format("{\"%s\":true, \"%s\":\"%s\"}", EVENT_REFRESH_TABLE, EVENT_SHOW_WARNING, escapeJson(message));
        return ResponseEntity.ok().header(HX_TRIGGER, json).build();
    }

    /**
     * Helper to manually build complex triggers if needed.
     */
    public static ResponseEntity<Void> okWithTrigger(String json) {
        return ResponseEntity.ok().header(HX_TRIGGER, json).build();
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"");
    }
}
