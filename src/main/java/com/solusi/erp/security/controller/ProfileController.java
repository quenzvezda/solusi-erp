package com.solusi.erp.security.controller;

import com.solusi.erp.security.dto.ProfileRequest;
import com.solusi.erp.security.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("profile", userService.getProfile(userDetails.getUsername()));
        return "security/profile/view";
    }

    @GetMapping("/edit")
    public String showEditForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (!model.containsAttribute("profileRequest")) {
            model.addAttribute("profileRequest", userService.getProfileUpdateData(userDetails.getUsername()));
        }
        return "security/profile/form";
    }

    @PostMapping("/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                               @Valid @ModelAttribute("profileRequest") ProfileRequest request,
                               BindingResult bindingResult,
                               HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "security/profile/form";
        }

        try {
            userService.updateProfile(userDetails.getUsername(), request);
            
            // Update locale in session
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(httpServletRequest);
            if (localeResolver != null) {
                localeResolver.setLocale(httpServletRequest, httpServletResponse, new Locale(request.getLanguageCode()));
            }

            redirectAttributes.addFlashAttribute("successMessage", "Profil berhasil diperbarui");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("profileRequest", request);
            return "redirect:/profile/edit";
        }
    }
}
