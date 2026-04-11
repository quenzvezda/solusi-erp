package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderLine;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreatePurchaseOrderUseCaseImpl implements CreatePurchaseOrderUseCase {

    private final PurchaseOrderRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreatePurchaseOrderUseCaseImpl(PurchaseOrderRepository repository,
                                          SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public PurchaseOrder execute(LocalDate orderDate, LocalDate expectedDate,
                                  Long supplierId, Long facilityId, Long currencyId,
                                  BigDecimal exchangeRate, int paymentTermDays,
                                  Long prId, String note, List<PoLineInput> lines) {
        String code = sequenceGeneratorService.generate("PO");

        List<PurchaseOrderLine> domainLines = lines != null
                ? lines.stream().map(this::toLine).toList()
                : List.of();

        PurchaseOrder po = PurchaseOrder.createNew(
                code, orderDate, expectedDate, supplierId, facilityId,
                currencyId, exchangeRate, paymentTermDays, prId, note, domainLines
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
