package com.solusi.erp.master.service;

import com.solusi.erp.master.dto.CurrencyDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CurrencyService {

    Page<CurrencyDto> getAllCurrencies(String keyword, Pageable pageable);

    CurrencyDto getCurrencyById(Long id);

    CurrencyDto createCurrency(CurrencyDto dto);

    CurrencyDto updateCurrency(Long id, CurrencyDto dto);

    void deleteCurrency(Long id);
}
