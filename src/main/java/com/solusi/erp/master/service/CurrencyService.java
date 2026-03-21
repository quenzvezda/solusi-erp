package com.solusi.erp.master.service;

import com.solusi.erp.master.dto.CurrencyRequest;
import com.solusi.erp.master.dto.CurrencyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CurrencyService {

    Page<CurrencyResponse> getAllCurrencies(String keyword, Pageable pageable);

    CurrencyResponse getCurrencyById(Long id);

    CurrencyRequest getEditData(Long id);

    CurrencyResponse createCurrency(CurrencyRequest request);

    CurrencyResponse updateCurrency(Long id, CurrencyRequest request);

    void deleteCurrency(Long id);

    List<CurrencyResponse> findAllActive();

    CurrencyResponse getDefaultCurrency();
}
