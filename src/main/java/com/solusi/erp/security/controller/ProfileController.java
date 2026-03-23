package com.solusi.erp.security.controller;

import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.security.dto.ProfileRequest;
import com.solusi.erp.security.dto.ProfileResponse;
import com.solusi.erp.security.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("profile", userService.getProfile(userDetails.getUsername()));
        return "security/profile/view";
    }

    @GetMapping("/edit")
    public String showEditForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        var viewDto = userService.getProfileEditView(userDetails.getUsername());
        model.addAttribute("profileRequest", viewDto.getRequest());
        model.addAttribute("auditInfo", viewDto.getAudit());
        return "security/profile/form";
    }

    @PostMapping("/edit")
    @ResponseBody
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        
        ProfileResponse data = userService.updateProfile(userDetails.getUsername(), request);

        // Update locale in browser (Cookie)
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(httpServletRequest);
        if (localeResolver != null) {
            localeResolver.setLocale(httpServletRequest, httpServletResponse, Locale.of(request.getLanguageCode()));
        }

        String msg = messageSource.getMessage("msg.profile.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }
}
