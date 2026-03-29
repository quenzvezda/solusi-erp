package com.solusi.erp.security.user.application.port;

public interface PasswordCipher {
    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
