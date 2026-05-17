package com.solusi.erp.master.bankaccount.web.controller;

import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.bankaccount.application.usecase.query.FindBankAccountsUseCase;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.domain.port.BankAccountLookupProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lookup/master/bank-accounts")
@RequiredArgsConstructor
public class BankAccountLookupController {

    private final FindBankAccountsUseCase findBankAccountsUseCase;
    private final BankAccountLookupProvider bankAccountLookupProvider;

    @GetMapping
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q,
                                  @RequestParam(value = "limit", defaultValue = "10") int limit,
                                  @RequestParam(value = "currencyId", required = false) Long currencyId,
                                  @RequestParam(value = "hasCoaOnly", required = false) Boolean hasCoaOnly) {
        List<BankAccount> results = findBankAccountsUseCase.execute(q, new Pageable(0, limit, "id", "ASC")).content();
        return results.stream()
                .filter(ba -> currencyId == null || currencyId.equals(ba.getCurrencyId()))
                .filter(ba -> !Boolean.TRUE.equals(hasCoaOnly) || ba.getCoaId() != null)
                .map(this::toLookupDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public LookupDto detail(@PathVariable Long id) {
        return bankAccountLookupProvider.resolve(id);
    }

    private LookupDto toLookupDto(BankAccount ba) {
        return bankAccountLookupProvider.resolve(ba.getId());
    }
}
