package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderLine;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderType;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreatePurchaseOrderUseCaseImpl implements CreatePurchaseOrderUseCase {

    private final PurchaseOrderRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final PurchaseRequisitionRepository purchaseRequisitionRepository;

    public CreatePurchaseOrderUseCaseImpl(PurchaseOrderRepository repository,
                                          SequenceGeneratorService sequenceGeneratorService,
                                          PurchaseRequisitionRepository purchaseRequisitionRepository) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.purchaseRequisitionRepository = purchaseRequisitionRepository;
    }

    @Override
    public PurchaseOrder execute(LocalDate orderDate, LocalDate expectedDate,
                                   Long supplierId, Long facilityId, Long currencyId,
                                   BigDecimal exchangeRate, int paymentTermDays,
                                   Long prId, PurchaseOrderType poType,
                                   Long taxId, String taxCode, String taxName, BigDecimal taxRate,
                                   TaxCalculationMode taxCalculationMode,
                                   String note, List<PoLineInput> lines) {
        PurchaseOrderType effectivePoType = poType != null ? poType : PurchaseOrderType.DIRECT;
        Long normalizedPrId = effectivePoType == PurchaseOrderType.STANDARD ? prId : null;
        List<PoLineInput> normalizedLines = lines != null ? lines : List.of();
        if (normalizedLines.isEmpty()) {
            throw new DomainException("msg.error.po.save.no.lines");
        }

        if (effectivePoType == PurchaseOrderType.STANDARD) {
            if (normalizedPrId == null) {
                throw new DomainException("msg.error.po.standard.pr.required");
            }
            new StandardPurchaseOrderValidator(purchaseRequisitionRepository, repository)
                    .validate(normalizedPrId, supplierId, facilityId, currencyId, normalizedLines);
        }

        String code = sequenceGeneratorService.generate("PO");

        List<PurchaseOrderLine> domainLines = normalizedLines.stream().map(this::toLine).toList();

        PurchaseOrder po = PurchaseOrder.createNew(
                code, orderDate, expectedDate, supplierId, facilityId,
                currencyId, exchangeRate, paymentTermDays, normalizedPrId, effectivePoType,
                taxId, taxCode, taxName, taxRate, taxCalculationMode,
                note, domainLines
        );
        return repository.save(po);
    }

    private PurchaseOrderLine toLine(PoLineInput input) {
        return new PurchaseOrderLine(
                AuditMetadata.empty(), null,
                input.productId(), input.quantity(),
                BigDecimal.ZERO, input.uomId(),
                input.unitPrice(), input.taxRate(),
                input.prLineId(), input.note()
        );
    }
}
