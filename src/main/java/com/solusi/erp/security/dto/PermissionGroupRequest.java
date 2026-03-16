package com.solusi.erp.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionGroupRequest {
    private Long id;

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 50, message = "{validation.size}")
    private String code;

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 100, message = "{validation.size}")
    private String nameId;

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 100, message = "{validation.size}")
    private String nameEn;

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 255, message = "{validation.size}")
    private String breadcrumbId;

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 255, message = "{validation.size}")
    private String breadcrumbEn;

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 255, message = "{validation.size}")
    private String urlPath;

    @Size(max = 255, message = "{validation.size}")
    private String descriptionId;

    @Size(max = 255, message = "{validation.size}")
    private String descriptionEn;
}
