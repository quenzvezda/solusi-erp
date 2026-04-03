package com.solusi.erp.master.bankaccount.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;

import java.util.Optional;

/**
 * Domain Repository interface for BankAccount.
 * Pure Java — no framework dependency.
 */
public interface BankAccountRepository {
    BankAccount save(BankAccount bankAccount);
    Optional<BankAccount> findById(Long id);
    Page<BankAccount> findAll(String keyword, Pageable pageable);
    void delete(Long id);
}
