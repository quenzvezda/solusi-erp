package com.solusi.erp.master.service;

import com.solusi.erp.master.dto.BankAccountRequestDto;
import com.solusi.erp.master.dto.BankAccountResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BankAccountService {
    Page<BankAccountResponseDto> findAll(String keyword, Pageable pageable);
    BankAccountResponseDto findById(Long id);
    BankAccountRequestDto getEditData(Long id);
    void create(BankAccountRequestDto request);
    void update(Long id, BankAccountRequestDto request);
    void delete(Long id);
}
