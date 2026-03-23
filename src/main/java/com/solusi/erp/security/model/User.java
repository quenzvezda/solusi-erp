package com.solusi.erp.security.model;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.master.model.Party;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Credentials Entity.
 * Mandate: 1 User has exactly 1 Role.
 * 
 * Mandate: AGENTS.md Section 5
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseModel {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "password_change_required", nullable = false)
    private boolean passwordChangeRequired = true;

    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile profile;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", unique = true)
    private Party party;
}
