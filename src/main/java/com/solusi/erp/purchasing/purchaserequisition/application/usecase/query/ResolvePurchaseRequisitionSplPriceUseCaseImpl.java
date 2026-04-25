package com.solusi.erp.purchasing.purchaserequisition.application.usecase.query;

import com.solusi.erp.purchasing.purchaserequisition.web.dto.api.SplPriceResponse;
import com.solusi.erp.purchasing.supplierpricelist.domain.service.SupplierPriceListResolutionService;

import java.time.LocalDate;
import java.util.Optional;

public class ResolvePurchaseRequisitionSplPriceUseCaseImpl implements ResolvePurchaseRequisitionSplPriceUseCase {

    private final SupplierPriceListResolutionService resolutionService;

    public ResolvePurchaseRequisitionSplPriceUseCaseImpl(SupplierPriceListResolutionService resolutionService) {
        this.resolutionService = resolutionService;
    }

    @Override
    public Optional<SplPriceResponse> execute(Long supplierId,
                                              Long productId,
                                              Long uomId,
                                              Long currencyId,
                                              LocalDate requiredDate) {
        LocalDate asOfDate = requiredDate != null ? requiredDate : LocalDate.now();
        return resolutionService.resolveActivePrice(supplierId, productId, uomId, currencyId, asOfDate)
                .map(spl -> new SplPriceResponse(
                        spl.getId(),
                        spl.getCode(),
                        spl.getUnitPrice(),
                        spl.getMinQuantity(),
                        spl.getSupplierId(),
                        spl.getProductId(),
                        spl.getUomId(),
                        spl.getCurrencyId(),
                        spl.getEffectiveFrom(),
                        spl.getEffectiveTo(),
                        spl.isActive()
                ));
    }
}
