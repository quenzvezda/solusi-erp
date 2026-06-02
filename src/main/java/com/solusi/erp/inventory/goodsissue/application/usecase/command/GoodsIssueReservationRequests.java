package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueLine;
import com.solusi.erp.inventory.stock.domain.model.InventoryReservationRequest;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class GoodsIssueReservationRequests {

    private GoodsIssueReservationRequests() {
    }

    static List<InventoryReservationRequest> from(List<GoodsIssueLine> lines) {
        return lines.stream()
                .filter(GoodsIssueLine::hasIssueQuantity)
                .flatMap(line -> from(line).stream())
                .toList();
    }

    private static List<InventoryReservationRequest> from(GoodsIssueLine line) {
        ReferenceType referenceType = ReferenceType.valueOf(line.getValuationRefType());
        if (!Boolean.TRUE.equals(line.getSerialized())) {
            return List.of(request(line, null, referenceType, line.getBaseQuantity()));
        }

        List<InventoryReservationRequest> requests = new ArrayList<>();
        Arrays.stream(line.getSerialNumber() == null ? new String[0] : line.getSerialNumber().split(","))
                .map(String::trim)
                .filter(serial -> !serial.isEmpty())
                .forEach(serial -> requests.add(request(line, serial, referenceType, BigDecimal.ONE)));
        return requests;
    }

    private static InventoryReservationRequest request(GoodsIssueLine line,
                                                       String serialNumber,
                                                       ReferenceType referenceType,
                                                       BigDecimal quantity) {
        return new InventoryReservationRequest(
                line.getProductId(),
                line.getFacilityId(),
                line.getGridId(),
                line.getContainerId(),
                Boolean.TRUE.equals(line.getSerialized()),
                serialNumber,
                referenceType,
                line.getValuationRefId(),
                line.getValuationRefLineId(),
                quantity
        );
    }
}
