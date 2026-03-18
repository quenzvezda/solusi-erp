package com.solusi.erp.master.service.impl;

import com.solusi.erp.master.dto.CurrencyRequest;
import com.solusi.erp.master.dto.CurrencyResponse;
import com.solusi.erp.master.mapper.CurrencyMapper;
import com.solusi.erp.master.model.Currency;
import com.solusi.erp.master.repository.CurrencyRepository;
import com.solusi.erp.master.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CurrencyResponse> getAllCurrencies(String keyword, Pageable pageable) {
        Page<Currency> page;
        if (StringUtils.hasText(keyword)) {
            page = currencyRepository.search(keyword, pageable);
        } else {
            page = currencyRepository.findAll(pageable);
        }
        return page.map(currencyMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyResponse getCurrencyById(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Currency not found"));
        return currencyMapper.toResponse(currency);
    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyRequest getEditData(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Currency not found"));
        return currencyMapper.toRequest(currency);
    }

    @Override
    @Transactional
    public void createCurrency(CurrencyRequest request) {
        if (currencyRepository.findByAlias(request.getAlias()).isPresent()) {
            throw new RuntimeException("Currency alias already exists");
        }
        Currency currency = currencyMapper.toEntity(request);
        handleDefaultStatus(currency);
        currencyRepository.save(currency);
    }

    @Override
    @Transactional
    public void updateCurrency(Long id, CurrencyRequest request) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Currency not found"));

        currencyRepository.findByAlias(request.getAlias())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException("Currency alias already exists");
                    }
                });

        currencyMapper.updateEntityFromRequest(request, currency);
        handleDefaultStatus(currency);
        currencyRepository.save(currency);
    }

    @Override
    @Transactional
    public void deleteCurrency(Long id) {
        if (!currencyRepository.existsById(id)) {
            throw new RuntimeException("Currency not found");
        }
        currencyRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurrencyResponse> findAllActive() {
        return currencyRepository.findAll().stream()
                .filter(c -> c.getIsActive() != null && c.getIsActive())
                .map(currencyMapper::toResponse)
                .toList();
    }

    private void handleDefaultStatus(Currency currency) {
        if (currency.getIsDefault() != null && currency.getIsDefault()) {
            List<Currency> defaults = currencyRepository.findByIsDefaultTrue();
            for (Currency prevDefault : defaults) {
                if (!prevDefault.getId().equals(currency.getId())) {
                    prevDefault.setIsDefault(false);
                    currencyRepository.save(prevDefault);
                }
            }
        } else if (currency.getIsDefault() == null) {
            currency.setIsDefault(false);
        }
    }
}
