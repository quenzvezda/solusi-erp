package com.solusi.erp.master.geographic.application.usecase.command;

import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.shared.model.GeographicType;

@FunctionalInterface
public interface UpdateGeographicUseCase {
    Geographic execute(Long id, String name, GeographicType type,
                       Long parentId, String parentName, Boolean isActive);
}

