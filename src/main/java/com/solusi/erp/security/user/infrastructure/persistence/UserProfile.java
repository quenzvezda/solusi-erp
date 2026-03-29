package com.solusi.erp.security.user.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * User Profile and UI Preferences Entity.
 * Linked 1-to-1 with the User Entity.
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile extends BaseModel {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "avatar_path", length = 255)
    private String avatarPath;

    @Column(name = "language_code", nullable = false, length = 5)
    private String languageCode = "id";

    @Column(name = "default_page_size", nullable = false)
    private Integer defaultPageSize = 10;

    @Column(nullable = false, length = 20)
    private String theme = "light";
}
