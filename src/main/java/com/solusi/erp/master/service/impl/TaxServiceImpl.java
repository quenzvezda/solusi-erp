package com.solusi.erp.master.service.impl;

import com.solusi.erp.master.dto.TaxDto;
import com.solusi.erp.master.mapper.TaxMapper;
import com.solusi.erp.master.model.Tax;
import com.solusi.erp.master.repository.TaxRepository;
import com.solusi.erp.master.service.TaxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxServiceImpl implements TaxService {

    private final TaxRepository taxRepository;
    private final TaxMapper taxMapper;
    private final MessageSource messageSource;

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaxDto> getAllTaxes(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return taxRepository.search(keyword, pageable).map(taxMapper::toDto);
        }
        return taxRepository.findAll(pageable).map(taxMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TaxDto getTaxById(Long id) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("error.tax.not.found")));
        return taxMapper.toDto(tax);
    }

    @Override
    @Transactional
    public TaxDto createTax(TaxDto dto) {
        // Validate duplicate code
        if (taxRepository.findByCode(dto.getCode()).isPresent()) {
            throw new RuntimeException(getMessage("error.tax.duplicate-code"));
        }

        Tax tax = taxMapper.toEntity(dto);
        if (tax.getIsActive() == null) {
            tax.setIsActive(false);
        }
        if (tax.getIsSubtract() == null) {
            tax.setIsSubtract(false);
        }

        Tax savedTax = taxRepository.save(tax);
        log.info("Created Tax with code: {}", savedTax.getCode());
        return taxMapper.toDto(savedTax);
    }

    @Override
    @Transactional
    public TaxDto updateTax(Long id, TaxDto dto) {
        Tax existingTax = taxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("error.tax.not.found")));

        taxMapper.updateEntityFromDto(dto, existingTax);
        if (existingTax.getIsSubtract() == null) {
            existingTax.setIsSubtract(false);
        }
        if (existingTax.getIsActive() == null) {
            existingTax.setIsActive(false);
        }

        Tax updatedTax = taxRepository.save(existingTax);
        log.info("Updated Tax with code: {}", updatedTax.getCode());
        return taxMapper.toDto(updatedTax);
    }

    @Override
    @Transactional
    public void deleteTax(Long id) {
        Tax existingTax = taxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("error.tax.not.found")));

        // Soft delete
        existingTax.setIsActive(false);
        taxRepository.save(existingTax);
        log.info("Soft deleted Tax with code: {}", existingTax.getCode());
    }
}
