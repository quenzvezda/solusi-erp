package com.solusi.erp.security.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a User.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserRequest extends BaseAuditResponse {

    @NotBlank(message = "{label.users.username} {validation.notblank.suffix}")
    @Size(min = 3, max = 50, message = "{label.users.username} {validation.size.suffix}")
    private String username;

    @NotBlank(message = "{label.users.email} {validation.notblank.suffix}")
    @Email(message = "{label.users.email} {validation.email.suffix}")
    private String email;

    private String password; // Optional on update, validated manually

    @NotNull(message = "{label.users.role} {validation.notnull.suffix}")
    private Long roleId;

    @NotBlank(message = "{label.users.fullname} {validation.notblank.suffix}")
    @Size(max = 100, message = "{label.users.fullname} {validation.size.suffix}")
    private String fullName;

    @Size(max = 20, message = "{label.users.phone} {validation.size.suffix}")
    private String phoneNumber;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Boolean passwordChangeRequired = false;
}
