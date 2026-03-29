package com.solusi.erp.security.user.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void createNew_defaultsPasswordChangeRequired() {
        User user = User.createNew("john", "enc", "john@test.com", 1L,
                UserProfile.createDefault("John", "0812"), true, null);

        assertThat(user.getUsername()).isEqualTo("john");
        assertThat(user.isPasswordChangeRequired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void toggleEnabled_flipsStatus() {
        User user = User.createNew("john", "enc", "john@test.com", 1L,
                UserProfile.createDefault("John", "0812"), true, null);

        user.toggleEnabled();

        assertThat(user.isEnabled()).isFalse();
    }
}
