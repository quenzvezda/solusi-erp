package com.solusi.erp.inventory.container.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.container.domain.port.ContainerLookupProvider;
import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerJpaRepository;

public class ContainerLookupProviderImpl implements ContainerLookupProvider {

    private final ContainerJpaRepository containerJpaRepository;

    public ContainerLookupProviderImpl(ContainerJpaRepository containerJpaRepository) {
        this.containerJpaRepository = containerJpaRepository;
    }

    @Override
    public LookupDto resolve(Long containerId) {
        if (containerId == null) return null;
        return containerJpaRepository.findById(containerId)
                .map(e -> new LookupDto(e.getId(), e.getName(), e.getCode()))
                .orElse(null);
    }
}
