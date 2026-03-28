package com.solusi.erp.master.currency.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.currency.domain.model.Currency;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository interface for Currency.
 * Pure Java — no framework dependency.
 */
public interface CurrencyRepository {
    Currency save(Currency currency);
    Optional<Currency> findById(Long id);
    Page<Currency> findAll(String keyword, Pageable pageable);
    void delete(Long id);
    boolean existsByAlias(String alias);
    List<Currency> findByIsDefaultTrue();
    List<Currency> findByIsActiveTrue();
}
