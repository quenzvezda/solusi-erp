package com.solusi.erp.inventory.goodsissue.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsIssueCancelLineRequest {

    private Long goodsIssueLineId;

    @NotNull(message = "{validation.gi.cancel.originalMovement.required}")
    private Long originalMovementId;

    @NotNull(message = "{validation.gi.cancel.targetContainer.required}")
    private Long targetContainerId;

    private String productLabel;

    private String serialNumber;

    private BigDecimal quantityIssued;
}
