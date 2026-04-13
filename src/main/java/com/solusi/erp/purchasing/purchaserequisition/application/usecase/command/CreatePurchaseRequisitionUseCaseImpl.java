package com.solusi.erp.purchasing.purchaserequisition.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionLine;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;

import java.time.LocalDate;
import java.util.List;

public class CreatePurchaseRequisitionUseCaseImpl implements CreatePurchaseRequisitionUseCase {

    private final PurchaseRequisitionRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreatePurchaseRequisitionUseCaseImpl(PurchaseRequisitionRepository repository,
                                                SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public PurchaseRequisition execute(LocalDate requestDate, Long requesterId, Long facilityId,
                                        String department, PurchaseRequisitionPriority priority,
                                        String note, Long suggestedSupplierId, List<LineInput> lines) {
        String code = sequenceGeneratorService.generate("PR");

        List<PurchaseRequisitionLine> domainLines = lines != null
                ? lines.stream().map(this::toLine).toList()
                : List.of();

        PurchaseRequisition pr = PurchaseRequisition.createNew(
                code, requestDate, requesterId, facilityId,
                department, priority, note, suggestedSupplierId, domainLines
        );
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
