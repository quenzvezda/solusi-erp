package com.solusi.erp.security.user.domain.model;

public class UserProfile {
    private String fullName;
    private String phoneNumber;
    private String avatarPath;
    private String languageCode;
    private Integer defaultPageSize;
    private String theme;

    public UserProfile(
            String fullName,
            String phoneNumber,
            String avatarPath,
            String languageCode,
            Integer defaultPageSize,
            String theme) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.avatarPath = avatarPath;
        this.languageCode = normalizeLanguage(languageCode);
        this.defaultPageSize = defaultPageSize == null ? 10 : defaultPageSize;
        this.theme = normalizeTheme(theme);
    }

    public static UserProfile createDefault(String fullName, String phoneNumber) {
        return new UserProfile(fullName, phoneNumber, null, "id", 10, "light");
    }

    public void update(String fullName, String phoneNumber, String languageCode, Integer defaultPageSize, String theme) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.languageCode = normalizeLanguage(languageCode);
        this.defaultPageSize = defaultPageSize == null ? this.defaultPageSize : defaultPageSize;
        this.theme = normalizeTheme(theme);
    }

    private String normalizeLanguage(String value) {
        return (value == null || value.isBlank()) ? "id" : value;
    }

    private String normalizeTheme(String value) {
        return (value == null || value.isBlank()) ? "light" : value;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public Integer getDefaultPageSize() {
        return defaultPageSize;
    }

    public String getTheme() {
        return theme;
    }
}

