package com.solusi.erp.security.user.web.controller;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.security.user.service.UserService;
import com.solusi.erp.security.user.web.dto.ProfileSaveRequest;
import com.solusi.erp.security.user.web.dto.ProfileResponse;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.User;
import org.springframework.ui.ExtendedModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProfileControllerTest {

    private final UserService userService = mock(UserService.class);
    private final MessageSource messageSource = mock(MessageSource.class);
    private final ProfileController controller = new ProfileController(userService, messageSource);

    @Test
    void showProfile_returnsViewTemplate() {
        when(userService.getProfile("john")).thenReturn(new ProfileResponse());

        String view = controller.showProfile(User.withUsername("john").password("n/a").authorities("A").build(), new ExtendedModelMap());

        assertThat(view).isEqualTo("security/profile/view");
    }

    @Test
    void showEditForm_returnsFormTemplate() {
        when(userService.getProfileEditView("john")).thenReturn(new FormViewDto<>(new ProfileSaveRequest(), null, new ProfileResponse()));

        String view = controller.showEditForm(User.withUsername("john").password("n/a").authorities("A").build(), new ExtendedModelMap());

        assertThat(view).isEqualTo("security/profile/form");
    }
}
