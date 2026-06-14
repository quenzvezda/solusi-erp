package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;

import java.time.LocalDate;
import java.util.List;

public class UpdatePurchaseReturnUseCaseImpl implements UpdatePurchaseReturnUseCase {

    private final PurchaseReturnRepository repository;
    private final PurchaseReturnDraftLineFactory lineFactory;

    public UpdatePurchaseReturnUseCaseImpl(PurchaseReturnRepository repository,
                                           PurchaseReturnSourceQueryPort sourceQueryPort) {
        this.repository = repository;
        this.lineFactory = new PurchaseReturnDraftLineFactory(sourceQueryPort);
    }

    @Override
    public PurchaseReturn execute(Long id, LocalDate returnDate, PurchaseReturnReason reason,
                                  String note, List<PurchaseReturnLineCommand> lines) {
        PurchaseReturn purchaseReturn = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.not-found"));
        if (!purchaseReturn.getStatus().canEdit()) {
            throw new DomainException("msg.error.purchase-return.update.not-draft");
        }
        lineFactory.requireSource(purchaseReturn.getReferenceId());
        purchaseReturn.updateDraft(
                returnDate,
                reason,
                note,
                lineFactory.buildLines(purchaseReturn.getReferenceId(), lines)
        );
        return repository.save(purchaseReturn);
    }
}
