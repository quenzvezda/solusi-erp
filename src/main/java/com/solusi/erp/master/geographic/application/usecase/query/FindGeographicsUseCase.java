package com.solusi.erp.master.geographic.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.geographic.domain.model.Geographic;

@FunctionalInterface
public interface FindGeographicsUseCase {
    Page<Geographic> execute(String keyword, Long parentId, Pageable pageable);
}
