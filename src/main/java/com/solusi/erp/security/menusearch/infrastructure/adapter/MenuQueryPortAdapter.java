package com.solusi.erp.security.menusearch.infrastructure.adapter;

import com.solusi.erp.security.menusearch.application.port.MenuQueryPort;
import com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroup;
import com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroupJpaRepository;
import org.springframework.data.domain.PageRequest;

import java.util.Collection;
import java.util.List;

public class MenuQueryPortAdapter implements MenuQueryPort {

    private final PermissionGroupJpaRepository repository;

    public MenuQueryPortAdapter(PermissionGroupJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<MenuItem> searchAllowedMenus(String keyword, Collection<String> authorities, int limit) {
        return repository.searchAllowedMenus(keyword, authorities, PageRequest.of(0, limit))
                .getContent().stream()
                .map(this::toMenuItem)
                .toList();
    }

    @Override
    public List<MenuItem> findAllByAuthorities(Collection<String> authorities) {
        return repository.findAllByAuthorities(authorities).stream()
                .map(this::toMenuItem)
                .toList();
    }

    private MenuItem toMenuItem(PermissionGroup pg) {
        return new MenuItem(
                pg.getUrlPath(),
                pg.getLocalizedName(),
                pg.getLocalizedBreadcrumb(),
                pg.getIconClass(),
                pg.getBreadcrumbId(),
                pg.getBreadcrumbEn()
        );
    }
}
