package com.solusi.erp.core.infrastructure.web.interceptor;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Interceptor to automatically inject audit information into the Model.
 * Runs AFTER the controller method (postHandle) to ensure all attributes 
 * added by the controller are available.
 * 
 * Scans the model for any attribute that inherits from BaseAuditResponse
 * and provides a generic 'auditInfo' alias for Thymeleaf fragments.
 */
@Component
public class AuditInfoInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler, ModelAndView modelAndView) throws Exception {
        
        if (modelAndView != null && modelAndView.getModelMap() != null) {
            Map<String, Object> modelMap = modelAndView.getModelMap();
            
            // Only proceed if auditInfo is not already manually set
            if (!modelMap.containsKey("auditInfo")) {
                modelMap.values().stream()
                        .filter(value -> value instanceof BaseAuditResponse)
                        .map(value -> (BaseAuditResponse) value)
                        .findFirst()
                        .ifPresent(auditData -> modelMap.put("auditInfo", auditData));
            }
        }
    }
}
