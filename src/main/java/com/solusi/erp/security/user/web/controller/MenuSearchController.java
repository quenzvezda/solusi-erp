package com.solusi.erp.security.user.web.controller;

import com.solusi.erp.core.dto.MenuSearchDto;
import com.solusi.erp.security.service.MenuSearchService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/menus")
public class MenuSearchController {

    private final MenuSearchService menuSearchService;

    public MenuSearchController(MenuSearchService menuSearchService) {
        this.menuSearchService = menuSearchService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MenuSearchDto> searchMenus(@RequestParam(defaultValue = "") String q,
                                           @RequestParam(defaultValue = "10") int limit) {
        return menuSearchService.searchMenus(q, limit);
    }
}
