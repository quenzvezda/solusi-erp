package com.solusi.erp.security.controller;

import com.solusi.erp.security.model.SecurityUser;
import com.solusi.erp.security.model.User;
import com.solusi.erp.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/reset-password")
    public String showResetPage() {
        return "security/reset-password";
    }

    @PostMapping("/reset-password")
    public String processReset(@AuthenticationPrincipal SecurityUser securityUser,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Password dan konfirmasi tidak cocok.");
            return "redirect:/reset-password";
        }

        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password minimal 6 karakter.");
            return "redirect:/reset-password";
        }

        User user = securityUser.user();
        user.setPassword(passwordEncoder.encode(password));
        user.setPasswordChangeRequired(false);
        user.setLastPasswordChange(LocalDateTime.now());
        
        userRepository.save(user);

        return "redirect:/dashboard?resetSuccess=true";
    }
}
