package com.solusi.erp.security.model;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "permission_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionGroup extends BaseModel {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name_id", nullable = false, length = 100)
    private String nameId;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "breadcrumb_id", nullable = false, length = 255)
    private String breadcrumbId;

    @Column(name = "breadcrumb_en", nullable = false, length = 255)
    private String breadcrumbEn;

    @Column(name = "url_path", nullable = false, length = 255)
    private String urlPath;

    @Column(name = "description_id", length = 255)
    private String descriptionId;

    @Column(name = "description_en", length = 255)
    private String descriptionEn;

    @OneToMany(mappedBy = "permissionGroup")
    private List<Permission> permissions;

    /**
     * Helper to get localized name based on current context locale
     */
    public String getLocalizedName() {
        Locale locale = LocaleContextHolder.getLocale();
        return "en".equals(locale.getLanguage()) ? nameEn : nameId;
    }

    /**
     * Helper to get localized breadcrumb based on current context locale
     */
    public String getLocalizedBreadcrumb() {
        Locale locale = LocaleContextHolder.getLocale();
        return "en".equals(locale.getLanguage()) ? breadcrumbEn : breadcrumbId;
    }
}
