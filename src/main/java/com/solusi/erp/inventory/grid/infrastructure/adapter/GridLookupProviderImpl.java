package com.solusi.erp.inventory.grid.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.grid.domain.port.GridLookupProvider;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridEntity;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridJpaRepository;

/**
 * Infrastructure adapter that resolves Grid into LookupDto with consistent subText.
 * SubText format: "{code}" — consistent with GridLookupController response.
 * This is the single source of truth for Grid autocomplete pre-edit representation.
 */
public class GridLookupProviderImpl implements GridLookupProvider {

    private final GridJpaRepository gridJpaRepository;

    public GridLookupProviderImpl(GridJpaRepository gridJpaRepository) {
        this.gridJpaRepository = gridJpaRepository;
    }

    @Override
    public LookupDto resolve(Long gridId) {
        if (gridId == null) return null;
        return gridJpaRepository.findById(gridId)
                .map(this::toLookupDto)
                .orElse(null);
    }

    private LookupDto toLookupDto(GridEntity entity) {
        return new LookupDto(entity.getId(), entity.getName(), entity.getCode());
    }
}
