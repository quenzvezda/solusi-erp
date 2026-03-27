package com.solusi.erp.inventory.facility.application.usecase.command;

import com.solusi.erp.inventory.facility.domain.model.Facility;

@FunctionalInterface
public interface CreateFacilityUseCase {
    Facility execute(String name, Long ownerId, String addressLine1, Long cityId, String postalCode, String note, Boolean isActive);
}
