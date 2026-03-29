package com.solusi.erp.security.user.infrastructure.adapter;

import com.solusi.erp.security.user.application.port.PasswordCipher;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordCipherAdapter implements PasswordCipher {

    private final PasswordEncoder passwordEncoder;

    public PasswordCipherAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
