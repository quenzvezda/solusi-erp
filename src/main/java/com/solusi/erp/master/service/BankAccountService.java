package com.solusi.erp.master.service;

import com.solusi.erp.master.dto.BankAccountRequest;
import com.solusi.erp.master.dto.BankAccountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BankAccountService {
    Page<BankAccountResponse> findAll(String keyword, Pageable pageable);
    BankAccountResponse findById(Long id);
    BankAccountRequest getEditData(Long id);
    void create(BankAccountRequest request);
    void update(Long id, BankAccountRequest request);
    void delete(Long id);
}
