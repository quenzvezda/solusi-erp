package com.solusi.erp.security.menusearch.application.port;

import java.util.Collection;
import java.util.List;

/**
 * Port for querying menu-related PermissionGroup data.
 * Implemented in infrastructure layer using JPA repository.
 */
public interface MenuQueryPort {

    record MenuItem(
            String urlPath,
            String localizedName,
            String localizedBreadcrumb,
            String iconClass,
            String breadcrumbId,
            String breadcrumbEn
    ) {}

    List<MenuItem> searchAllowedMenus(String keyword, Collection<String> authorities, int limit);

    List<MenuItem> findAllByAuthorities(Collection<String> authorities);
}
