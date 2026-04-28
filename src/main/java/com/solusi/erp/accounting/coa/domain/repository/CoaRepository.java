package com.solusi.erp.accounting.coa.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;

import java.util.List;
import java.util.Optional;

public interface CoaRepository {
    ChartOfAccount save(ChartOfAccount coa);
    Optional<ChartOfAccount> findById(Long id);
    Page<ChartOfAccount> findAll(String keyword, Pageable pageable);
    void delete(Long id);
    boolean existsByCode(String code);
    List<ChartOfAccount> search(String keyword, int limit);
    List<ChartOfAccount> findAllActive();
}
