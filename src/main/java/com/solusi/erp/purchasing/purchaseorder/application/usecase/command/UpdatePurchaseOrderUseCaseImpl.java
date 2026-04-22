package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderLine;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderType;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class UpdatePurchaseOrderUseCaseImpl implements UpdatePurchaseOrderUseCase {

    private final PurchaseOrderRepository repository;
    private final PurchaseRequisitionRepository purchaseRequisitionRepository;

    public UpdatePurchaseOrderUseCaseImpl(PurchaseOrderRepository repository,
                                          PurchaseRequisitionRepository purchaseRequisitionRepository) {
        this.repository = repository;
        this.purchaseRequisitionRepository = purchaseRequisitionRepository;
    }

    @Override
    public PurchaseOrder execute(Long id, LocalDate orderDate, LocalDate expectedDate,
                                  Long facilityId, Long currencyId, BigDecimal exchangeRate,
                                  int paymentTermDays, String note, List<PoLineInput> lines) {
        PurchaseOrder po = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.po.notfound"));

        List<PurchaseOrderLine> domainLines = lines != null
                ? lines.stream().map(this::toLine).toList()
                : List.of();
        if (domainLines.isEmpty()) {
            throw new DomainException("msg.error.po.save.no.lines");
        }

        if (po.getPoType() == PurchaseOrderType.STANDARD && po.getPrId() != null) {
            new StandardPurchaseOrderValidator(purchaseRequisitionRepository, repository)
                    .validate(po.getPrId(), po.getSupplierId(), facilityId, currencyId, lines);
        }

        po.update(orderDate, expectedDate, facilityId, currencyId,
                exchangeRate, paymentTermDays, note, domainLines);
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
