package com.solusi.erp.security.service;

import com.solusi.erp.security.model.SecurityUser;
import com.solusi.erp.security.model.UserProfile;
import com.solusi.erp.security.repository.UserProfileRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.io.IOException;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserProfileRepository userProfileRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        UserProfile profile = securityUser.user().getProfile();

        if (profile != null) {
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            if (localeResolver != null) {
                // 1. Ambil locale saat ini (bisa dari Cookie 'lang' yang dipilih di landing page)
                Locale currentLocale = localeResolver.resolveLocale(request);
                String currentLang = currentLocale.getLanguage();

                // 2. Jika di Cookie ada bahasa (User pilih sebelum login), tapi beda dengan DB
                if (!currentLang.equals(profile.getLanguageCode())) {
                    // Update Database sesuai pilihan browser terakhir
                    profile.setLanguageCode(currentLang);
                    userProfileRepository.save(profile);
                } 
                // 3. Jika di Cookie tidak ada/default, gunakan preferensi dari DB
                else if (profile.getLanguageCode() != null) {
                    localeResolver.setLocale(request, response, new Locale(profile.getLanguageCode()));
                }
            }
        }

        setDefaultTargetUrl("/dashboard");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
