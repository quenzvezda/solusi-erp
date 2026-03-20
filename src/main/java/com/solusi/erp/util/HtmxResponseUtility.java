package com.solusi.erp.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

/**
 * Utility for standardizing HTMX responses in controllers.
 * Reduces boilerplate code for HX headers and fragment selection.
 */
public class HtmxResponseUtility {

    private static final String HX_REDIRECT = "HX-Redirect";
    private static final String GLOBAL_ERROR_FRAGMENT = "fragments/alerts :: error";

    /**
     * Set the HX-Redirect header to tell HTMX to redirect the browser.
     * @param response The HTTP response
     * @param url Target URL
     * @return null (Standard for HTMX redirect responses in Spring MVC)
     */
    public static String redirect(HttpServletResponse response, String url) {
        response.setHeader(HX_REDIRECT, url);
        return null;
    }

    /**
     * Return the global error fragment for HTMX requests.
     * @return Fragment path
     */
    public static String returnErrorFragment() {
        return GLOBAL_ERROR_FRAGMENT;
    }

    /**
     * Helper to handle exceptions in HTMX requests.
     * @param model UI Model
     * @param message Error message
     * @return Fragment path
     */
    public static String handleException(Model model, String message) {
        model.addAttribute("errorMessage", message);
        return GLOBAL_ERROR_FRAGMENT;
    }
}
