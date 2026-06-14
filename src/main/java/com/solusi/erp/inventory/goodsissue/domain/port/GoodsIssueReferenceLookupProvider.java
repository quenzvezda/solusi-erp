package com.solusi.erp.inventory.goodsissue.domain.port;

import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GoodsIssueReferenceLookupProvider {

    String resolveReferenceCode(GoodsIssueReferenceType referenceType, Long referenceId);

    Map<Long, ReferenceLineSnapshot> resolveReferenceLineSnapshots(GoodsIssueReferenceType referenceType,
                                                                    Long referenceId);

    default boolean supportsSourceLineSelector(GoodsIssueReferenceType referenceType) {
        return false;
    }

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
                                  String uomCode,
                                  String uomName,
                                  Long facilityId,
                                  String facilityCode,
                                  String facilityName,
                                  Long gridId,
                                  String gridCode,
                                  String gridName,
                                  Long containerId,
                                  String containerCode,
                                  String containerName,
                                  boolean serialized,
                                  BigDecimal unitCost,
                                  BigDecimal inventoryAmount,
                                  BigDecimal taxAmount,
                                  String valuationRefType,
                                  Long valuationRefId,
                                  Long valuationRefLineId) {
    }
}
