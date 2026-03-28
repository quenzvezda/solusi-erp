package com.solusi.erp.master.geographic.application.usecase.command;

import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.model.GeographicType;

@FunctionalInterface
public interface CreateGeographicUseCase {
    Geographic execute(String code, String name, GeographicType type,
                       Long parentId, String parentName, Boolean isActive);
}
