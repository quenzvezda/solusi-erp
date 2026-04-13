package com.solusi.erp.purchasing.purchaserequisition.application.usecase.command;

import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;

import java.time.LocalDate;
import java.util.List;

@FunctionalInterface
public interface CreatePurchaseRequisitionUseCase {
    PurchaseRequisition execute(LocalDate requestDate, Long requesterId, Long facilityId,
                                String department, PurchaseRequisitionPriority priority,
                                String note, Long suggestedSupplierId, Long currencyId, List<LineInput> lines);
}
