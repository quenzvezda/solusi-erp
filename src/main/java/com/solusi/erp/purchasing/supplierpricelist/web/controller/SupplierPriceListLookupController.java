package com.solusi.erp.purchasing.supplierpricelist.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lookup/purchasing/supplier-price-lists")
@RequiredArgsConstructor
public class SupplierPriceListLookupController {

    private final SupplierPriceListRepository supplierPriceListDomainRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('LOOKUP_SUPPLIER-PRICE-LIST')")
    public List<LookupDto> search(@RequestParam(required = false) String keyword,
                                   @RequestParam(defaultValue = "10") int limit) {
        return supplierPriceListDomainRepository.search(keyword, limit).stream()
            .map(spl -> new LookupDto(
                spl.getId(),
                spl.getCode(),
                "Price: " + spl.getUnitPrice().toPlainString(),
                Map.of(
                    "unitPrice", spl.getUnitPrice(),
                    "supplierId", spl.getSupplierId(),
                    "productId", spl.getProductId(),
                    "uomId", spl.getUomId(),
                    "currencyId", spl.getCurrencyId()
                )
            ))
            .collect(Collectors.toList());
    }
}
