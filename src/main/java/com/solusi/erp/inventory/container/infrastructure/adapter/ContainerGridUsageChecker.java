package com.solusi.erp.inventory.container.infrastructure.adapter;

import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerJpaRepository;
import com.solusi.erp.inventory.grid.domain.port.GridUsageChecker;

public class ContainerGridUsageChecker implements GridUsageChecker {

    private final ContainerJpaRepository containerJpaRepository;

    public ContainerGridUsageChecker(ContainerJpaRepository containerJpaRepository) {
        this.containerJpaRepository = containerJpaRepository;
    }

    @Override
    public boolean isUsed(Long gridId) {
        return containerJpaRepository.existsByGridId(gridId);
    }
}
