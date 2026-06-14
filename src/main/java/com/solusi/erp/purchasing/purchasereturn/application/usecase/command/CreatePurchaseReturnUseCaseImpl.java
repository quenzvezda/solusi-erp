package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.EligibleGoodsReceiptRow;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;

import java.time.LocalDate;
import java.util.List;

public class CreatePurchaseReturnUseCaseImpl implements CreatePurchaseReturnUseCase {

    private final PurchaseReturnRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final PurchaseReturnDraftLineFactory lineFactory;

    public CreatePurchaseReturnUseCaseImpl(PurchaseReturnRepository repository,
                                           SequenceGeneratorService sequenceGeneratorService,
                                           PurchaseReturnSourceQueryPort sourceQueryPort) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.lineFactory = new PurchaseReturnDraftLineFactory(sourceQueryPort);
    }

    @Override
    public PurchaseReturn execute(Long goodsReceiptId, LocalDate returnDate, PurchaseReturnReason reason,
                                  String note, List<PurchaseReturnLineCommand> lines) {
        EligibleGoodsReceiptRow source = lineFactory.requireSource(goodsReceiptId);
        PurchaseReturn purchaseReturn = PurchaseReturn.createNew(
                sequenceGeneratorService.generate("PURCHASE_RETURN"),
                returnDate,
                PurchaseReturn.GOODS_RECEIPT_REFERENCE_TYPE,
                source.goodsReceiptId(),
                source.goodsReceiptCode(),
                source.purchaseOrderId(),
                source.purchaseOrderCode(),
                source.supplierId(),
                source.facilityId(),
                source.currencyId(),
                source.exchangeRate(),
                reason,
                note,
                lineFactory.buildLines(source.goodsReceiptId(), lines)
        );
        return repository.save(purchaseReturn);
    }
}
