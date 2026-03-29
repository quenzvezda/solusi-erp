package com.solusi.erp.core.infrastructure.web.advice;

import com.solusi.erp.security.role.infrastructure.persistence.Role;
import com.solusi.erp.security.shared.model.SecurityUser;
import com.solusi.erp.security.user.infrastructure.persistence.User;
import com.solusi.erp.security.user.infrastructure.persistence.UserProfile;
import com.solusi.erp.security.user.infrastructure.persistence.UserProfileJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalModelAttributeAdviceTest {

    private final UserProfileJpaRepository userProfileRepository = mock(UserProfileJpaRepository.class);
    private final GlobalModelAttributeAdvice advice = new GlobalModelAttributeAdvice(userProfileRepository);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addGlobalAttributes_authenticatedUser_addsProfile() {
        var securityUser = stubSecurityUser("john");
        var auth = new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        var profile = new UserProfile();
        when(userProfileRepository.findByUserUsername("john")).thenReturn(Optional.of(profile));

        Model model = new ExtendedModelMap();
        advice.addGlobalAttributes(model);

        assertThat(model.getAttribute("userProfile")).isSameAs(profile);
    }

    @Test
    void addGlobalAttributes_anonymous_doesNotAddProfile() {
        SecurityContextHolder.clearContext();

        Model model = new ExtendedModelMap();
        advice.addGlobalAttributes(model);

        assertThat(model.getAttribute("userProfile")).isNull();
        verifyNoInteractions(userProfileRepository);
    }

    @Test
    void addGlobalAttributes_nonSecurityUserPrincipal_doesNotAddProfile() {
        var auth = new UsernamePasswordAuthenticationToken("anonymousUser", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Model model = new ExtendedModelMap();
        advice.addGlobalAttributes(model);

        assertThat(model.getAttribute("userProfile")).isNull();
        verifyNoInteractions(userProfileRepository);
    }

    @Test
    void addGlobalAttributes_profileNotFound_doesNotAddProfile() {
        var securityUser = stubSecurityUser("unknown");
        var auth = new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userProfileRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

        Model model = new ExtendedModelMap();
        advice.addGlobalAttributes(model);

        assertThat(model.getAttribute("userProfile")).isNull();
    }

    private SecurityUser stubSecurityUser(String username) {
        var role = mock(Role.class);
        when(role.getPermissions()).thenReturn(Collections.emptySet());
        var user = mock(User.class);
        when(user.getUsername()).thenReturn(username);
        when(user.getRole()).thenReturn(role);
        return new SecurityUser(user);
    }
}
