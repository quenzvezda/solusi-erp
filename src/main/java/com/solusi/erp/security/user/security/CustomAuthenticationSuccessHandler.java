package com.solusi.erp.security.user.security;

import com.solusi.erp.security.shared.model.SecurityUser;
import com.solusi.erp.security.user.infrastructure.persistence.UserProfile;
import com.solusi.erp.security.menusearch.application.usecase.BuildMenuTreeUseCase;
import com.solusi.erp.security.user.infrastructure.persistence.UserProfileJpaRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserProfileJpaRepository userProfileRepository;
    private final BuildMenuTreeUseCase buildMenuTreeUseCase;

    public CustomAuthenticationSuccessHandler(UserProfileJpaRepository userProfileRepository,
                                              BuildMenuTreeUseCase buildMenuTreeUseCase) {
        this.userProfileRepository = userProfileRepository;
        this.buildMenuTreeUseCase = buildMenuTreeUseCase;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        UserProfile profile = securityUser.user().getProfile();

        Set<String> authorities = authentication.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        request.getSession().setAttribute("userMenu", buildMenuTreeUseCase.execute(authorities));

        if (profile != null) {
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            if (localeResolver != null) {
                Locale currentLocale = localeResolver.resolveLocale(request);
                String currentLang = currentLocale.getLanguage();

                if (!currentLang.equals(profile.getLanguageCode())) {
                    profile.setLanguageCode(currentLang);
                    userProfileRepository.save(profile);
                } else if (profile.getLanguageCode() != null) {
                    localeResolver.setLocale(request, response, Locale.of(profile.getLanguageCode()));
                }
            }
        }

        setDefaultTargetUrl("/dashboard");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
