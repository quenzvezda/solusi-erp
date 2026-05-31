package com.solusi.erp.config;

import com.solusi.erp.security.user.security.CustomAuthenticationSuccessHandler;
import com.solusi.erp.security.user.security.ForcePasswordChangeFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(classes = {
        SecurityConfig.class,
        LoginContinueRedirectIntegrationTest.TestSecurityBeans.class
})
class LoginContinueRedirectIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void staleSessionProtectedRequestContinuesToOriginalUrlAfterLogin() throws Exception {
        MvcResult protectedResult = mockMvc.perform(get("/purchasing/purchase-orders")
                        .with(staleSessionId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl()).endsWith("/login"))
                .andReturn();

        MockHttpSession savedRequestSession = (MockHttpSession) protectedResult.getRequest().getSession(false);
        assertThat(savedRequestSession).as("protected request should create a session for the saved request")
                .isNotNull();

        mockMvc.perform(post("/login")
                        .session(savedRequestSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "admin")
                        .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location",
                        "http://localhost/purchasing/purchase-orders?continue"));
    }

    private RequestPostProcessor staleSessionId() {
        return request -> {
            request.setRequestedSessionId("stale-session-id");
            request.setRequestedSessionIdValid(false);
            request.setRequestedSessionIdFromCookie(true);
            return request;
        };
    }

    @Configuration
    public static class TestSecurityBeans {

        @Bean
        ForcePasswordChangeFilter forcePasswordChangeFilter() {
            return new ForcePasswordChangeFilter();
        }

        @Bean
        LogoutAccessDeniedHandler logoutAccessDeniedHandler() {
            return new LogoutAccessDeniedHandler();
        }

        @Bean
        CustomAuthenticationSuccessHandler successHandler() {
            return new ContinueOnlyAuthenticationSuccessHandler();
        }

        @Bean
        UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
            return new InMemoryUserDetailsManager(User.withUsername("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .authorities("PURCHASE-ORDER_READ", "DASHBOARD_READ")
                    .build());
        }

        @Bean
        TestRoutes testRoutes() {
            return new TestRoutes();
        }
    }

    public static final class ContinueOnlyAuthenticationSuccessHandler extends CustomAuthenticationSuccessHandler {

        private final SavedRequestAwareAuthenticationSuccessHandler delegate =
                new SavedRequestAwareAuthenticationSuccessHandler();

        ContinueOnlyAuthenticationSuccessHandler() {
            super(null, null);
            delegate.setDefaultTargetUrl("/dashboard");
        }

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) throws IOException, ServletException {
            delegate.onAuthenticationSuccess(request, response, authentication);
        }
    }

    @RestController
    public static class TestRoutes {

        @GetMapping("/purchasing/purchase-orders")
        String purchaseOrders() {
            return "purchase-orders";
        }
    }
}
