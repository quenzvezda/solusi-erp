package com.solusi.erp.purchasing.purchaserequisition.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionLine;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;

import java.time.LocalDate;
import java.util.List;

public class UpdatePurchaseRequisitionUseCaseImpl implements UpdatePurchaseRequisitionUseCase {

    private final PurchaseRequisitionRepository repository;

    public UpdatePurchaseRequisitionUseCaseImpl(PurchaseRequisitionRepository repository) {
        this.repository = repository;
    }

    @Override
    public PurchaseRequisition execute(Long id, LocalDate requestDate, Long facilityId,
                                        String department, PurchaseRequisitionPriority priority,
                                        String note, Long suggestedSupplierId, Long currencyId, List<LineInput> lines) {
        PurchaseRequisition pr = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.pr.notfound"));

        List<PurchaseRequisitionLine> domainLines = lines != null
                ? lines.stream().map(this::toLine).toList()
                : List.of();

        pr.update(requestDate, facilityId, department, priority, note, suggestedSupplierId, currencyId, domainLines);
        return repository.save(pr);
    }

    private PurchaseRequisitionLine toLine(LineInput input) {
        return new PurchaseRequisitionLine(
                AuditMetadata.empty(), null,
                input.productId(), input.quantity(), input.uomId(),
                input.requiredDate(), input.estimatedUnitPrice(),
                null, input.note()
        );
    }
}
