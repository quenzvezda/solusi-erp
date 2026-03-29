package com.solusi.erp.core.infrastructure.web.resolver;

import com.solusi.erp.security.role.infrastructure.persistence.Role;
import com.solusi.erp.security.shared.model.SecurityUser;
import com.solusi.erp.security.user.infrastructure.persistence.User;
import com.solusi.erp.security.user.infrastructure.persistence.UserProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserPreferencePageableResolverTest {

    private final UserPreferencePageableResolver resolver = new UserPreferencePageableResolver();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolveArgument_withSizeParam_usesRequestSize() throws Exception {
        var request = new MockHttpServletRequest();
        request.addParameter("size", "25");
        request.addParameter("page", "0");

        Pageable result = resolver.resolveArgument(
                pageableMethodParam(), null, new ServletWebRequest(request), null);

        assertThat(result.getPageSize()).isEqualTo(25);
    }

    @Test
    void resolveArgument_noSize_authenticatedWithPreference_usesPreference() throws Exception {
        var profile = new UserProfile();
        profile.setDefaultPageSize(50);

        var securityUser = stubSecurityUser(profile);
        var auth = new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        var request = new MockHttpServletRequest();
        request.addParameter("page", "0");

        Pageable result = resolver.resolveArgument(
                pageableMethodParam(), null, new ServletWebRequest(request), null);

        assertThat(result.getPageSize()).isEqualTo(50);
    }

    @Test
    void resolveArgument_noSize_anonymous_usesDefault() throws Exception {
        SecurityContextHolder.clearContext();

        var request = new MockHttpServletRequest();
        request.addParameter("page", "0");

        Pageable result = resolver.resolveArgument(
                pageableMethodParam(), null, new ServletWebRequest(request), null);

        assertThat(result.getPageSize()).isEqualTo(20);
    }

    @Test
    void resolveArgument_noSize_authenticatedWithoutProfile_usesDefault() throws Exception {
        var securityUser = stubSecurityUser(null);
        var auth = new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        var request = new MockHttpServletRequest();
        request.addParameter("page", "0");

        Pageable result = resolver.resolveArgument(
                pageableMethodParam(), null, new ServletWebRequest(request), null);

        assertThat(result.getPageSize()).isEqualTo(20);
    }

    private SecurityUser stubSecurityUser(UserProfile profile) {
        var role = mock(Role.class);
        when(role.getPermissions()).thenReturn(Collections.emptySet());
        var user = mock(User.class);
        when(user.getRole()).thenReturn(role);
        when(user.getProfile()).thenReturn(profile);
        return new SecurityUser(user);
    }

    private MethodParameter pageableMethodParam() throws NoSuchMethodException {
        return new MethodParameter(
                DummyController.class.getMethod("list", Pageable.class), 0);
    }

    @SuppressWarnings("unused")
    static class DummyController {
        public void list(Pageable pageable) {
        }
    }
}
