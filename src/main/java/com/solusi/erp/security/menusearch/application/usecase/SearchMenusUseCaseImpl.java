package com.solusi.erp.security.menusearch.application.usecase;

import com.solusi.erp.core.dto.MenuSearchDto;
import com.solusi.erp.security.menusearch.application.port.MenuQueryPort;

import java.util.Collection;
import java.util.List;

public class SearchMenusUseCaseImpl implements SearchMenusUseCase {

    private final MenuQueryPort menuQueryPort;

    public SearchMenusUseCaseImpl(MenuQueryPort menuQueryPort) {
        this.menuQueryPort = menuQueryPort;
    }

    @Override
    public List<MenuSearchDto> execute(String keyword, int limit, Collection<String> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            return List.of();
        }
        return menuQueryPort.searchAllowedMenus(keyword, authorities, limit).stream()
                .map(item -> new MenuSearchDto(item.urlPath(), item.localizedName(), item.localizedBreadcrumb()))
                .toList();
    }
}
