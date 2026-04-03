package com.solusi.erp.security.menusearch.application.usecase;

import com.solusi.erp.security.menusearch.web.dto.MenuNodeResponse;
import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface BuildMenuTreeUseCase {
    List<MenuNodeResponse> execute(Collection<String> authorities);
}
