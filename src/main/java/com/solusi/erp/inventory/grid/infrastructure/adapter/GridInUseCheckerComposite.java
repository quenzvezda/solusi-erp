package com.solusi.erp.inventory.grid.infrastructure.adapter;

import com.solusi.erp.inventory.grid.domain.port.GridUsageChecker;

import java.util.List;

public class GridInUseCheckerComposite {

    private final List<GridUsageChecker> checkers;

    public GridInUseCheckerComposite(List<GridUsageChecker> checkers) {
        this.checkers = checkers;
    }

    public boolean isInUse(Long gridId) {
        return checkers.stream().anyMatch(c -> c.isUsed(gridId));
    }
}
