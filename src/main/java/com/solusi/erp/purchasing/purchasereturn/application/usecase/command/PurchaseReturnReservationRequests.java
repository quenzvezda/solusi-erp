package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.inventory.stock.domain.model.InventoryReservationRequest;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnLine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class PurchaseReturnReservationRequests {

    private PurchaseReturnReservationRequests() {
    }

    static List<InventoryReservationRequest> from(PurchaseReturn purchaseReturn) {
        return purchaseReturn.getLines().stream()
                .flatMap(line -> from(line).stream())
                .toList();
    }

    private static List<InventoryReservationRequest> from(PurchaseReturnLine line) {
        ReferenceType referenceType = ReferenceType.valueOf(line.getValuationReferenceType());
        if (!line.isSerialized()) {
            return List.of(request(line, null, referenceType, line.getBaseQuantity()));
        }

        List<InventoryReservationRequest> requests = new ArrayList<>();
        Arrays.stream(line.getSerialNumbers().split(","))
                .map(String::trim)
                .filter(serial -> !serial.isEmpty())
                .forEach(serial -> requests.add(request(line, serial, referenceType, BigDecimal.ONE)));
        return requests;
    }

    private static InventoryReservationRequest request(PurchaseReturnLine line,
                                                       String serialNumber,
                                                       ReferenceType referenceType,
                                                       BigDecimal quantity) {
        return new InventoryReservationRequest(
                line.getProductId(),
                line.getFacilityId(),
                line.getGridId(),
                line.getContainerId(),
                line.isSerialized(),
                serialNumber,
                referenceType,
                line.getValuationReferenceId(),
                line.getValuationReferenceLineId(),
                quantity
        );
    }
}
