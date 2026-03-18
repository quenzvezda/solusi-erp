package com.solusi.erp.inventory.dto;

import com.solusi.erp.inventory.model.MovementType;
import com.solusi.erp.inventory.model.ReferenceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payload for Stock Movement Requests (Core Utility).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementPayload {

    @NotNull(message = "{validation.product.id.required}")
    private Long productId;

    @NotNull(message = "{validation.container.id.required}")
    private Long containerId;

    private String serialNumber;

    @NotNull(message = "{validation.quantity.required}")
    @DecimalMin(value = "0.0001", message = "{validation.quantity.min}")
    private BigDecimal quantity;

    @NotNull(message = "{validation.movement_type.required}")
    private MovementType movementType;

    private ReferenceType referenceType;

    private Long referenceId;

    private String referenceCode;

    private BigDecimal netPrice;
    
    @Builder.Default
    private LocalDateTime transactionDate = LocalDateTime.now();
}
