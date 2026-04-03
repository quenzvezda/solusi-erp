package com.solusi.erp.security.permissiongroup.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

/**
 * Aggregate Root: PermissionGroup (Menu Group).
 * Pure Java Domain Model — no Spring, no JPA, no Lombok.
 */
public class PermissionGroup {
    private final AuditMetadata metadata;
    private String code;
    private String nameId;
    private String nameEn;
    private String breadcrumbId;
    private String breadcrumbEn;
    private String urlPath;
    private String iconClass;
    private String descriptionId;
    private String descriptionEn;

    public PermissionGroup(AuditMetadata metadata, String code, String nameId, String nameEn,
                           String breadcrumbId, String breadcrumbEn, String urlPath,
                           String iconClass, String descriptionId, String descriptionEn) {
        this.metadata = metadata;
        this.code = code;
        this.nameId = nameId;
        this.nameEn = nameEn;
        this.breadcrumbId = breadcrumbId;
        this.breadcrumbEn = breadcrumbEn;
        this.urlPath = urlPath;
        this.iconClass = iconClass;
        this.descriptionId = descriptionId;
        this.descriptionEn = descriptionEn;
    }

    public static PermissionGroup createNew(String code, String nameId, String nameEn,
                                            String breadcrumbId, String breadcrumbEn,
                                            String urlPath, String iconClass,
                                            String descriptionId, String descriptionEn) {
        return new PermissionGroup(AuditMetadata.empty(), code, nameId, nameEn,
                breadcrumbId, breadcrumbEn, urlPath, iconClass, descriptionId, descriptionEn);
    }

    public void update(String nameId, String nameEn, String breadcrumbId, String breadcrumbEn,
                       String urlPath, String iconClass, String descriptionId, String descriptionEn) {
        this.nameId = nameId;
        this.nameEn = nameEn;
        this.breadcrumbId = breadcrumbId;
        this.breadcrumbEn = breadcrumbEn;
        this.urlPath = urlPath;
        this.iconClass = iconClass;
        this.descriptionId = descriptionId;
        this.descriptionEn = descriptionEn;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getNameId() { return nameId; }
    public String getNameEn() { return nameEn; }
    public String getBreadcrumbId() { return breadcrumbId; }
    public String getBreadcrumbEn() { return breadcrumbEn; }
    public String getUrlPath() { return urlPath; }
    public String getIconClass() { return iconClass; }
    public String getDescriptionId() { return descriptionId; }
    public String getDescriptionEn() { return descriptionEn; }
}
