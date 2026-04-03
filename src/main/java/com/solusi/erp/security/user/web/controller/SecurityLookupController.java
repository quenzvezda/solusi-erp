package com.solusi.erp.security.user.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.security.role.application.usecase.query.GetRoleLookupUseCase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/security")
@PreAuthorize("hasAnyAuthority('USERS_READ', 'ROLES_READ')")
public class SecurityLookupController {

    private final GetRoleLookupUseCase getRoleLookupUseCase;

    public SecurityLookupController(GetRoleLookupUseCase getRoleLookupUseCase) {
        this.getRoleLookupUseCase = getRoleLookupUseCase;
    }

    @GetMapping("/roles")
    public List<LookupDto> lookupRoles(@RequestParam(required = false) String q,
                                       @RequestParam(defaultValue = "10") int limit) {
        return getRoleLookupUseCase.search(q, limit);
    }

    @GetMapping("/roles/{id}")
    public LookupDto getRole(@PathVariable Long id) {
        return getRoleLookupUseCase.getById(id);
    }
}
