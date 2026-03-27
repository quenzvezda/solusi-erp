package com.solusi.erp.inventory.uomconversion.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;

public interface FindUomConversionsUseCase {
    Page<UomConversion> execute(String keyword, Pageable pageable);
}
