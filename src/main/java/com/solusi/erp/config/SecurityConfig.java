package com.solusi.erp.config;

import com.solusi.erp.security.service.CustomAuthenticationSuccessHandler;
import com.solusi.erp.security.service.ForcePasswordChangeFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Main Security Configuration for the ERP System.
 * Implements Stateful / Session-based Security.
 * 
 * Mandate: AGENTS.md Section 2 & 5
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize("hasAuthority('...')")
@RequiredArgsConstructor
public class SecurityConfig {

    private final ForcePasswordChangeFilter forcePasswordChangeFilter;
    private final CustomAuthenticationSuccessHandler successHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .addFilterAfter(forcePasswordChangeFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // Publicly accessible paths (Assets, Landing Page & Login)
                .requestMatchers(
                    "/",
                    "/static/**",
                    "/libs/**",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/favicon.ico",
                    "/login",
                    "/error",
                    "/reset-password"
                ).permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // Mandate: AGENTS.md Section 5 (Stateful Security)
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            );

        return http.build();
    }
}
