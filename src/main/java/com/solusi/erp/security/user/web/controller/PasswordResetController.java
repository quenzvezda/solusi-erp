package com.solusi.erp.security.user.web.controller;

import com.solusi.erp.security.shared.model.SecurityUser;
import com.solusi.erp.security.user.web.dto.PasswordResetRequest;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class PasswordResetController {

    private final com.solusi.erp.security.user.infrastructure.persistence.UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    public PasswordResetController(com.solusi.erp.security.user.infrastructure.persistence.UserJpaRepository userJpaRepository,
                                   PasswordEncoder passwordEncoder,
                                   MessageSource messageSource) {
        this.userJpaRepository = userJpaRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
    }

    @GetMapping("/reset-password")
    public String showResetPage() {
        return "security/reset-password";
    }

    @PostMapping("/reset-password")
    public String processReset(@AuthenticationPrincipal SecurityUser securityUser,
                               @Valid PasswordResetRequest request,
                               RedirectAttributes redirectAttributes) {
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();
        if (!password.equals(confirmPassword)) {
            String error = messageSource.getMessage("msg.error.password.mismatch", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/reset-password";
        }
        if (password.length() < 6) {
            String error = messageSource.getMessage("msg.error.password.length", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/reset-password";
        }

        com.solusi.erp.security.user.infrastructure.persistence.User user = securityUser.user();
        user.setPassword(passwordEncoder.encode(password));
        user.setPasswordChangeRequired(false);
        user.setLastPasswordChange(LocalDateTime.now());
        userJpaRepository.save(user);

        return "redirect:/dashboard?resetSuccess=true";
    }
}
