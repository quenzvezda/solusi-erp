package com.solusi.erp.security.service;

import com.solusi.erp.core.dto.MenuSearchDto;
import com.solusi.erp.security.model.PermissionGroup;
import com.solusi.erp.security.repository.PermissionGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuSearchService {

    private final PermissionGroupRepository permissionGroupRepository;

    @Transactional(readOnly = true)
    public List<MenuSearchDto> searchMenus(String keyword, int limit) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }

        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (authorities.isEmpty()) {
            return List.of();
        }

        Pageable pageable = PageRequest.of(0, limit);
        Page<PermissionGroup> results = permissionGroupRepository.searchAllowedMenus(keyword, authorities, pageable);

        return results.getContent().stream()
                .map(pg -> new MenuSearchDto(pg.getUrlPath(), pg.getLocalizedName(), pg.getLocalizedBreadcrumb()))
                .toList();
    }
}
