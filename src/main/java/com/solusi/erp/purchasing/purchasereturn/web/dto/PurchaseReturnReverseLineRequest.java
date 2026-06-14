package com.solusi.erp.purchasing.purchasereturn.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReturnReverseLineRequest {

    @NotNull(message = "{validation.purchase-return.reverse.original-movement-required}")
    private Long originalMovementId;

    @NotNull(message = "{validation.purchase-return.reverse.target-container-required}")
    private Long targetContainerId;
}
