package com.solusi.erp.security.role.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.security.role.domain.port.RoleLookupProvider;
import com.solusi.erp.security.role.infrastructure.persistence.RoleJpaRepository;

public class RoleLookupProviderImpl implements RoleLookupProvider {

    private final RoleJpaRepository roleJpaRepository;

    public RoleLookupProviderImpl(RoleJpaRepository roleJpaRepository) {
        this.roleJpaRepository = roleJpaRepository;
    }

    @Override
    public LookupDto resolve(Long roleId) {
        if (roleId == null) return null;
        return roleJpaRepository.findById(roleId)
                .map(e -> new LookupDto(e.getId(), e.getName(), e.getDescription()))
                .orElse(null);
    }
}
