package com.solusi.erp.inventory.container.application.usecase.command;

import com.solusi.erp.inventory.container.domain.model.Container;
import java.math.BigDecimal;

@FunctionalInterface
public interface CreateContainerUseCase {
    Container execute(Long gridId, String name, String barcode, BigDecimal length, BigDecimal width,
                      BigDecimal height, BigDecimal maxWeight, String note, Boolean isActive);
}
