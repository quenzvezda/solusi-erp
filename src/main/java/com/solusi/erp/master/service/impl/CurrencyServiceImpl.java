package com.solusi.erp.master.service.impl;

import com.solusi.erp.master.dto.CurrencyRequest;
import com.solusi.erp.master.dto.CurrencyResponse;
import com.solusi.erp.master.mapper.CurrencyMapper;
import com.solusi.erp.master.model.Currency;
import com.solusi.erp.master.repository.CurrencyRepository;
import com.solusi.erp.master.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;
    private final MessageSource messageSource;

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CurrencyResponse> getAllCurrencies(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return currencyRepository.search(keyword, pageable).map(currencyMapper::toResponse);
        }
        return currencyRepository.findAll(pageable).map(currencyMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyResponse getCurrencyById(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("error.currency.not.found")));
        return currencyMapper.toResponse(currency);
    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyRequest getEditData(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("error.currency.not.found")));
        return currencyMapper.toRequest(currency);
    }

    @Override
    @Transactional
    public void createCurrency(CurrencyRequest request) {
        // Validate duplicate alias
        if (currencyRepository.findByAlias(request.getAlias()).isPresent()) {
            throw new RuntimeException(getMessage("error.currency.duplicate-alias"));
        }

        Currency currency = currencyMapper.toEntity(request);

        handleDefaultStatus(currency);

        if (currency.getIsActive() == null) {
            currency.setIsActive(false);
        }

        Currency savedCurrency = currencyRepository.save(currency);
        log.info("Created Currency with symbol: {}", savedCurrency.getSymbol());
    }

    @Override
    @Transactional
    public void updateCurrency(Long id, CurrencyRequest request) {
        Currency existingCurrency = currencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("error.currency.not.found")));

        // Validate duplicate alias if changed
        if (!existingCurrency.getAlias().equalsIgnoreCase(request.getAlias())) {
            if (currencyRepository.findByAlias(request.getAlias()).isPresent()) {
                throw new RuntimeException(getMessage("error.currency.duplicate-alias"));
            }
        }

        currencyMapper.updateEntityFromRequest(request, existingCurrency);

        handleDefaultStatus(existingCurrency);

        if (existingCurrency.getIsActive() == null) {
            existingCurrency.setIsActive(false);
        }

        Currency updatedCurrency = currencyRepository.save(existingCurrency);
        log.info("Updated Currency with symbol: {}", updatedCurrency.getSymbol());
    }

    @Override
    @Transactional
    public void deleteCurrency(Long id) {
        Currency existingCurrency = currencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("error.currency.not.found")));

        // Soft delete
        existingCurrency.setIsActive(false);
        if (existingCurrency.getIsDefault()) {
            existingCurrency.setIsDefault(false);
        }
        currencyRepository.save(existingCurrency);
        log.info("Soft deleted Currency with symbol: {}", existingCurrency.getSymbol());
    }

    private void handleDefaultStatus(Currency currency) {
        if (currency.getIsDefault() != null && currency.getIsDefault()) {
            List<Currency> existingDefaults = currencyRepository.findByIsDefaultTrue();
            for (Currency prevDefault : existingDefaults) {
                if (currency.getId() == null || !prevDefault.getId().equals(currency.getId())) {
                    prevDefault.setIsDefault(false);
                    currencyRepository.save(prevDefault);
                }
            }
        } else if (currency.getIsDefault() == null) {
            currency.setIsDefault(false);
        }
    }
}
