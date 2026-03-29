package com.solusi.erp.security.user.web.controller;

import com.solusi.erp.security.shared.model.SecurityUser;
import com.solusi.erp.security.user.infrastructure.persistence.User;
import com.solusi.erp.security.user.infrastructure.persistence.UserJpaRepository;
import com.solusi.erp.security.user.web.dto.PasswordResetRequest;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PasswordResetControllerTest {

    private final UserJpaRepository userJpaRepository = mock(UserJpaRepository.class);
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder = mock(org.springframework.security.crypto.password.PasswordEncoder.class);
    private final MessageSource messageSource = mock(MessageSource.class);
    private final PasswordResetController controller = new PasswordResetController(userJpaRepository, passwordEncoder, messageSource);

    @Test
    void showResetPage_returnsTemplate() {
        assertThat(controller.showResetPage()).isEqualTo("security/reset-password");
    }

    @Test
    void processReset_rejectsMismatch() {
        User user = new User();
        when(messageSource.getMessage(eq("msg.error.password.mismatch"), any(), any(Locale.class))).thenReturn("mismatch");

        PasswordResetRequest request = new PasswordResetRequest();
        request.setPassword("abc123");
        request.setConfirmPassword("zzz123");
        String view = controller.processReset(new SecurityUser(user), request, new RedirectAttributesModelMap());

        assertThat(view).isEqualTo("redirect:/reset-password");
        verify(userJpaRepository, never()).save(any());
    }
}
