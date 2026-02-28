package com.solusi.erp.config;

import com.solusi.erp.core.pagination.UserPreferencePageableResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Custom Web MVC Configuration.
 * Registers custom argument resolvers for dynamic features like pagination preferences.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserPreferencePageableResolver userPreferencePageableResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userPreferencePageableResolver);
    }
}
