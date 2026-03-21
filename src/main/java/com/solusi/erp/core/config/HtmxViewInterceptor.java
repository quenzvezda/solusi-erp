package com.solusi.erp.core.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor to automatically append Thymeleaf fragments for HTMX requests.
 * Uses the 'HX-Target' header to determine which fragment to render.
 */
@Component
public class HtmxViewInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (modelAndView != null && isHtmxRequest(request)) {
            String target = request.getHeader("HX-Target");
            String viewName = modelAndView.getViewName();

            // If it's an HTMX request and we have a target, and the view name doesn't already specify a fragment
            if (target != null && viewName != null && !viewName.contains("::") && !viewName.startsWith("redirect:")) {
                // Standardize: We expect the fragment name to match the target ID (without the #)
                modelAndView.setViewName(viewName + " :: " + target);
            }
        }
    }

    private boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }
}
