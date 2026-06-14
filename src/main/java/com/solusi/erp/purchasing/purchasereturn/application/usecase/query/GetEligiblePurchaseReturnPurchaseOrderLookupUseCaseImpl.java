package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;

import java.util.List;

public class GetEligiblePurchaseReturnPurchaseOrderLookupUseCaseImpl
        implements GetEligiblePurchaseReturnPurchaseOrderLookupUseCase {

    private final PurchaseReturnSourceQueryPort queryPort;

    public GetEligiblePurchaseReturnPurchaseOrderLookupUseCaseImpl(PurchaseReturnSourceQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    public List<LookupDto> execute(String query, int limit) {
        return queryPort.findEligiblePurchaseOrders(query, limit);
    }
}
