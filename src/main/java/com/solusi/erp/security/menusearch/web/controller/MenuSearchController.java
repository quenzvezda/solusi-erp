package com.solusi.erp.security.menusearch.web.controller;

import com.solusi.erp.core.dto.MenuSearchDto;
import com.solusi.erp.security.menusearch.application.usecase.SearchMenusUseCase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lookup/menus")
public class MenuSearchController {

    private final SearchMenusUseCase searchMenusUseCase;

    public MenuSearchController(SearchMenusUseCase searchMenusUseCase) {
        this.searchMenusUseCase = searchMenusUseCase;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MenuSearchDto> searchMenus(@RequestParam(defaultValue = "") String q,
                                           @RequestParam(defaultValue = "10") int limit) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return List.of();
        }
        Set<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return searchMenusUseCase.execute(q, limit, authorities);
    }
}
