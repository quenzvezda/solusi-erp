package com.solusi.erp.purchasing.supplierpricelist.domain.service;

import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Domain service untuk resolve SPL (Supplier Price List) dengan prioritas:
 * - Match: supplier + product + uom + currency
 * - Valid date range: effectiveFrom <= today <= effectiveTo (or effectiveTo is null)
 * - Active only: is_active = true
 * - Priority: Ambil yang effectiveFrom paling terbaru (latest)
 */
public class SupplierPriceListResolutionService {

    private final SupplierPriceListRepository repository;

    public SupplierPriceListResolutionService(SupplierPriceListRepository repository) {
        this.repository = repository;
    }

    /**
     * Resolve the best matching active SPL for the given criteria.
     * Returns the most recently effective (latest effectiveFrom) SPL that matches all criteria.
     *
     * @param supplierId Supplier ID
     * @param productId Product ID
     * @param uomId Unit of Measure ID
     * @param currencyId Currency ID
     * @param asOfDate Reference date for date range validation (typically today)
     * @return Optional containing the resolved SPL, or empty if none found
     */
    public Optional<SupplierPriceList> resolveActivePrice(Long supplierId, Long productId,
                                                           Long uomId, Long currencyId,
                                                           LocalDate asOfDate) {
        return repository.findMostRecentActiveSPL(supplierId, productId, uomId, currencyId, asOfDate);
    }
}
