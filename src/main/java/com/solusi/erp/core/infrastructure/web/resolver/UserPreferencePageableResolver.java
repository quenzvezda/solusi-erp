package com.solusi.erp.core.infrastructure.web.resolver;

import com.solusi.erp.security.shared.model.SecurityUser;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Custom Pageable Resolver that automatically applies user preferences.
 * If 'size' is not provided in request, it uses 'defaultPageSize' from UserProfile.
 */
@Component
public class UserPreferencePageableResolver extends PageableHandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Pageable.class.equals(parameter.getParameterType());
    }

    @Override
    public Pageable resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        
        // Let Spring's default resolver handle basic parsing (page, size, sort)
        Pageable defaultPageable = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        
        // If 'size' is present in request, respect it
        if (webRequest.getParameter("size") != null) {
            return defaultPageable;
        }

        // Otherwise, try to get preference from UserProfile
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityUser securityUser) {
            if (securityUser.user().getProfile() != null) {
                int pageSize = securityUser.user().getProfile().getDefaultPageSize();
                return PageRequest.of(defaultPageable.getPageNumber(), pageSize, defaultPageable.getSort());
            }
        }

        return defaultPageable;
    }
}
