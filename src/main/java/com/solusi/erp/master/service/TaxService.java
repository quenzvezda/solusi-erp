package com.solusi.erp.master.service;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.master.dto.TaxRequest;
import com.solusi.erp.master.dto.TaxResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaxService {
    Page<TaxResponse> getAllTaxes(String keyword, Pageable pageable);

    TaxResponse getTaxById(Long id);

    TaxRequest getEditData(Long id);

    FormViewDto<TaxRequest, Void, TaxResponse> getTaxEditView(Long id);

    TaxResponse createTax(TaxRequest request);

    TaxResponse updateTax(Long id, TaxRequest request);

    void deleteTax(Long id);
}
