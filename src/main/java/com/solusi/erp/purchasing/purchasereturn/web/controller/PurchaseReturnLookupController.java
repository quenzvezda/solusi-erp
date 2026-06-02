package com.solusi.erp.purchasing.purchasereturn.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetEligiblePurchaseReturnPurchaseOrderLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/purchasing/purchase-return-source-pos")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PURCHASE-RETURN_CREATE')")
public class PurchaseReturnLookupController {

    private final GetEligiblePurchaseReturnPurchaseOrderLookupUseCase lookupUseCase;

    @GetMapping
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String query,
                                  @RequestParam(defaultValue = "10") int limit) {
        return lookupUseCase.execute(query, limit);
    }
}
