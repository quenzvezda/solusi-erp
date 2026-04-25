package com.solusi.erp.purchasing.purchaserequisition.application.usecase.query;

import com.solusi.erp.purchasing.purchaserequisition.web.dto.api.SplPriceResponse;

import java.time.LocalDate;
import java.util.Optional;

public interface ResolvePurchaseRequisitionSplPriceUseCase {

    Optional<SplPriceResponse> execute(Long supplierId,
                                       Long productId,
                                       Long uomId,
                                       Long currencyId,
                                       LocalDate requiredDate);
}
