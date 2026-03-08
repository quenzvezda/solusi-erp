package com.solusi.erp.master.service;

import com.solusi.erp.master.dto.TaxDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaxService {
    Page<TaxDto> getAllTaxes(String keyword, Pageable pageable);

    TaxDto getTaxById(Long id);

    TaxDto createTax(TaxDto dto);

    TaxDto updateTax(Long id, TaxDto dto);

    void deleteTax(Long id);
}
