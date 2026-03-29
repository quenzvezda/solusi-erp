package com.solusi.erp.security.menusearch.application.usecase;

import com.solusi.erp.core.dto.MenuSearchDto;
import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface SearchMenusUseCase {
    List<MenuSearchDto> execute(String keyword, int limit, Collection<String> authorities);
}
