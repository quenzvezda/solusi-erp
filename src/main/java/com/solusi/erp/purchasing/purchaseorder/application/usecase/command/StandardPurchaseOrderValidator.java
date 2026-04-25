package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionLine;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class StandardPurchaseOrderValidator {

    private final PurchaseRequisitionRepository purchaseRequisitionRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    StandardPurchaseOrderValidator(PurchaseRequisitionRepository purchaseRequisitionRepository,
                                   PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseRequisitionRepository = purchaseRequisitionRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    void validate(Long prId,
                  Long supplierId,
                  Long facilityId,
                  Long currencyId,
                  List<PoLineInput> lines) {
        PurchaseRequisition pr = purchaseRequisitionRepository.findById(prId)
                .orElseThrow(() -> new DomainException("msg.error.po.standard.pr.not.found"));
        if (pr.getStatus() != PurchaseRequisitionStatus.APPROVED) {
            throw new DomainException("msg.error.po.standard.pr.not.approved");
        }
        if (!Objects.equals(pr.getSuggestedSupplierId(), supplierId)
                || !Objects.equals(pr.getFacilityId(), facilityId)
                || !Objects.equals(pr.getCurrencyId(), currencyId)) {
            throw new DomainException("msg.error.po.standard.header.mismatch");
        }
        if (lines == null || lines.isEmpty()) {
            return;
        }

        Map<Long, PurchaseRequisitionLine> prLinesById = pr.getLines().stream()
                .collect(Collectors.toMap(PurchaseRequisitionLine::getId, line -> line));
        Map<Long, BigDecimal> requestedQuantityByPrLineId = new HashMap<>();

        for (PoLineInput line : lines) {
            if (line.prLineId() == null) {
                throw new DomainException("msg.error.po.standard.line.prLine.required");
            }
            PurchaseRequisitionLine prLine = prLinesById.get(line.prLineId());
            if (prLine == null
                    || !Objects.equals(prLine.getProductId(), line.productId())
                    || !Objects.equals(prLine.getUomId(), line.uomId())) {
                throw new DomainException("msg.error.po.standard.line.invalid.reference");
            }
            if (requestedQuantityByPrLineId.putIfAbsent(line.prLineId(), line.quantity()) != null) {
                throw new DomainException("msg.error.po.standard.line.duplicate");
            }
        }

        Map<Long, BigDecimal> committedQuantityByPrLineId =
                purchaseOrderRepository.sumCommittedQuantityByPrLineIds(requestedQuantityByPrLineId.keySet());

        for (Map.Entry<Long, BigDecimal> entry : requestedQuantityByPrLineId.entrySet()) {
            PurchaseRequisitionLine prLine = prLinesById.get(entry.getKey());
            BigDecimal committed = committedQuantityByPrLineId.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            BigDecimal remaining = prLine.getQuantity().subtract(committed);
            if (entry.getValue().compareTo(remaining) > 0) {
                throw new DomainException("msg.error.po.standard.line.quantity.exceeds.remaining");
            }
        }
    }
}
