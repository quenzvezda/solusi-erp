package com.solusi.erp.master.currency.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.currency.infrastructure.persistence.Currency;
import com.solusi.erp.master.currency.infrastructure.persistence.CurrencyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/master/currencies")
@RequiredArgsConstructor
public class CurrencyLookupController {

    private final CurrencyJpaRepository currencyJpaRepository;

    @GetMapping
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q,
                                  @RequestParam(value = "limit", defaultValue = "10") int limit) {
        if (q.isEmpty()) {
            return currencyJpaRepository.findByIsActiveTrue().stream()
                    .map(this::toLookupDto)
                    .limit(limit)
                    .toList();
        }
        return currencyJpaRepository.search(q, PageRequest.of(0, limit))
                .map(this::toLookupDto)
                .toList();
    }

    private LookupDto toLookupDto(Currency c) {
        return new LookupDto(c.getId(), c.getName(), c.getSymbol() + " - " + c.getAlias());
    }
}
