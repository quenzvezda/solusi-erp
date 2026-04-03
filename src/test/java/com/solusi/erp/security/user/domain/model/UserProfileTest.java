package com.solusi.erp.security.user.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileTest {

    @Test
    void createDefault_setsDefaultLanguagePageSizeTheme() {
        UserProfile profile = UserProfile.createDefault("John Doe", "0812");

        assertThat(profile.getFullName()).isEqualTo("John Doe");
        assertThat(profile.getLanguageCode()).isEqualTo("id");
        assertThat(profile.getDefaultPageSize()).isEqualTo(10);
        assertThat(profile.getTheme()).isEqualTo("light");
    }

    @Test
    void update_normalizesLanguageAndTheme() {
        UserProfile profile = UserProfile.createDefault("John", "0812");

        profile.update("Jane", "0822", "", 25, "");

        assertThat(profile.getFullName()).isEqualTo("Jane");
        assertThat(profile.getPhoneNumber()).isEqualTo("0822");
        assertThat(profile.getLanguageCode()).isEqualTo("id");
        assertThat(profile.getDefaultPageSize()).isEqualTo(25);
        assertThat(profile.getTheme()).isEqualTo("light");
    }
}
