package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;

import java.util.List;

public interface GetEligiblePurchaseReturnPurchaseOrderLookupUseCase {

    List<LookupDto> execute(String query, int limit);
}
