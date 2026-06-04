package com.solusi.erp.inventory.stock.domain.port;

import com.solusi.erp.inventory.stock.domain.model.StockMovementReversalRequest;

import java.util.List;

public interface StockMovementReversalService {

    void reverse(List<StockMovementReversalRequest> requests);
}
