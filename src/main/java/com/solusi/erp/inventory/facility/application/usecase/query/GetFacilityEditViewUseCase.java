package com.solusi.erp.inventory.facility.application.usecase.query;

import com.solusi.erp.inventory.facility.domain.model.Facility;
import java.util.Optional;

@FunctionalInterface
public interface GetFacilityEditViewUseCase {
    Optional<Facility> execute(Long id);
}
