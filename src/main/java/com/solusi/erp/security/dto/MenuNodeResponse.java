package com.solusi.erp.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Hierarchical Menu Structure.
 * Used for dynamic sidebar rendering.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuNodeResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nameId;
    private String nameEn;
    private String urlPath;
    private String iconClass;
    
    public String getName() {
        java.util.Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale();
        return "en".equals(locale.getLanguage()) ? nameEn : nameId;
    }
    
    @Builder.Default
    private List<MenuNodeResponse> children = new ArrayList<>();

    public void addChild(MenuNodeResponse child) {
        this.children.add(child);
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }
}
