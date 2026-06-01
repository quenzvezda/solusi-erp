package com.solusi.erp.inventory.goodsissue.domain.port;

import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GoodsIssueReferenceLookupProvider {

    String resolveReferenceCode(GoodsIssueReferenceType referenceType, Long referenceId);

    Map<Long, ReferenceLineSnapshot> resolveReferenceLineSnapshots(GoodsIssueReferenceType referenceType,
                                                                   Long referenceId);

    List<SourceLineSelectorRow> resolveSourceLineSelectorRows(GoodsIssueReferenceType referenceType,
                                                              Long referenceId);

    record ReferenceLineSnapshot(BigDecimal sourceQuantity,
                                 BigDecimal issuedToDateQuantity,
                                 BigDecimal remainingQuantity,
                                 BigDecimal unitCost,
                                 String valuationRefType,
                                 Long valuationRefId,
                                 Long valuationRefLineId) {
    }

    record SourceLineSelectorRow(Long referenceLineId,
                                 Long productId,
                                 String productCode,
                                 String productName,
                                 BigDecimal sourceQuantity,
                                 BigDecimal issuedToDateQuantity,
                                 BigDecimal remainingQuantity,
                                 Long uomId,
                                 BigDecimal unitCost,
                                 String valuationRefType,
                                 Long valuationRefId,
                                 Long valuationRefLineId) {
    }
}
