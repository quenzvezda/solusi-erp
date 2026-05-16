package com.solusi.erp.master.bankaccount.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccount;
import com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lookup/master/bank-accounts")
@RequiredArgsConstructor
public class BankAccountLookupController {

    private final BankAccountJpaRepository bankAccountJpaRepository;

    @GetMapping
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q,
                                  @RequestParam(value = "limit", defaultValue = "10") int limit,
                                  @RequestParam(value = "currencyId", required = false) Long currencyId,
                                  @RequestParam(value = "hasCoaOnly", required = false) Boolean hasCoaOnly) {
        List<BankAccount> results;
        if (q.isEmpty()) {
            results = bankAccountJpaRepository.findByIsActiveTrue(PageRequest.of(0, limit)).getContent();
        } else {
            results = bankAccountJpaRepository.search(q, PageRequest.of(0, limit)).getContent();
        }
        return results.stream()
                .filter(ba -> currencyId == null || currencyId.equals(ba.getCurrencyId()))
                .filter(ba -> !Boolean.TRUE.equals(hasCoaOnly) || ba.getCoaId() != null)
                .map(this::toLookupDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public LookupDto detail(@PathVariable Long id) {
        return bankAccountJpaRepository.findById(id)
                .map(this::toLookupDto)
                .orElse(null);
    }

    private LookupDto toLookupDto(BankAccount ba) {
        String subText = ba.getCode() + " - " + ba.getBankName();
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentType", ba.getAccountType() != null ? ba.getAccountType().name() : null);
        payload.put("currencyId", ba.getCurrencyId());
        payload.put("coaId", ba.getCoaId());
        return new LookupDto(ba.getId(), ba.getAccountName(), subText, payload);
    }
}
