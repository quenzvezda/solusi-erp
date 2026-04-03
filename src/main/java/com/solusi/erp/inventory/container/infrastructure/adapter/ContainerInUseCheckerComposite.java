package com.solusi.erp.inventory.container.infrastructure.adapter;

import com.solusi.erp.inventory.container.domain.port.ContainerUsageChecker;

import java.util.List;

/**
 * Aggregates all ContainerUsageChecker beans registered by consumer slices.
 * Returns true if any checker reports the container is in use.
 */
public class ContainerInUseCheckerComposite {

    private final List<ContainerUsageChecker> checkers;

    public ContainerInUseCheckerComposite(List<ContainerUsageChecker> checkers) {
        this.checkers = checkers;
    }

    public boolean isUsed(Long containerId) {
        return checkers.stream().anyMatch(c -> c.isUsed(containerId));
    }
}
