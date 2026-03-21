package com.solusi.erp.core.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor to automatically handle HTMX requests.
 * 1. Appends fragments based on 'HX-Target'.
 * 2. Converts standard redirects to 'HX-Redirect' headers.
 * 3. Handles 'In-place' actions (like delete) by returning empty responses.
 */
@Component
public class HtmxViewInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (!isHtmxRequest(request) || modelAndView == null) {
            return;
        }

        String target = request.getHeader("HX-Target");
        String viewName = modelAndView.getViewName();

        if (viewName == null) return;

        // 1. Handle Redirects for HTMX
        if (viewName.startsWith("redirect:")) {
            String redirectUrl = viewName.substring(9);
            
            // OPTIMIZATION: If HTMX is targeting a specific element (like a table row), 
            // and NOT the main content area or body, we assume it's an "In-place" action (like delete).
            // In this case, we return an empty body (200 OK) to let HTMX remove the element.
            if (target != null && !target.equals("body") && !target.equals("main-content")) {
                modelAndView.clear();
                response.setStatus(HttpServletResponse.SC_OK);
                // SIGNAL: Tell the table to refresh itself
                response.setHeader("HX-Trigger", "refresh-table");
                return;
            }

            // Otherwise, tell HTMX to perform a full page redirect
            response.setHeader("HX-Redirect", redirectUrl);
            modelAndView.clear();
            return;
        }

        // 2. Handle Magic Fragment Selection
        if (target != null && !viewName.contains("::")) {
            // Standardize: We expect the fragment name to match the target ID
            modelAndView.setViewName(viewName + " :: " + target);
        }
    }

    private boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }
}
