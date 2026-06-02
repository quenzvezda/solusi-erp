package com.solusi.erp.purchasing.purchasereturn.web.dto;

import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PurchaseReturnLineDetailResponse {

    private Long id;
    private Long goodsReceiptLineId;
    private Long productId;
    private String productName;
    private String productCode;
    private boolean serialized;
    private BigDecimal quantity;
    private Long uomId;
    private String uomName;
    private String uomCode;
    private BigDecimal baseQuantity;
    private Long facilityId;
    private String facilityName;
    private Long gridId;
    private String gridName;
    private String gridCode;
    private Long containerId;
    private String containerName;
    private String containerCode;
    private String serialNumbers;
    private PurchaseReturnReason reason;
    private String note;
    private String valuationReferenceType;
    private Long valuationReferenceId;
    private Long valuationReferenceLineId;
    private BigDecimal unitCost;
    private BigDecimal inventoryAmount;
    private BigDecimal taxReversalAmount;
    private BigDecimal clearingAmount;
}
