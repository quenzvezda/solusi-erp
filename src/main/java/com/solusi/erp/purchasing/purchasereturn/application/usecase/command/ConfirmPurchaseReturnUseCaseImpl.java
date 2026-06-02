package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CompleteGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.port.PurchaseReturnGoodsIssueSourcePort;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.service.GoodsIssueSourceResolverRegistry;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;

public class ConfirmPurchaseReturnUseCaseImpl implements ConfirmPurchaseReturnUseCase {

    private final PurchaseReturnRepository purchaseReturnRepository;
    private final PurchaseReturnGoodsIssueSourcePort sourcePort;
    private final GoodsIssueSourceResolverRegistry resolverRegistry;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final GoodsIssueRepository goodsIssueRepository;
    private final CompleteGoodsIssueUseCase completeGoodsIssueUseCase;
    private final EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;

    public ConfirmPurchaseReturnUseCaseImpl(PurchaseReturnRepository purchaseReturnRepository,
                                            PurchaseReturnGoodsIssueSourcePort sourcePort,
                                            GoodsIssueSourceResolverRegistry resolverRegistry,
                                            SequenceGeneratorService sequenceGeneratorService,
                                            GoodsIssueRepository goodsIssueRepository,
                                            CompleteGoodsIssueUseCase completeGoodsIssueUseCase,
                                            EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase) {
        this.purchaseReturnRepository = purchaseReturnRepository;
        this.sourcePort = sourcePort;
        this.resolverRegistry = resolverRegistry;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.goodsIssueRepository = goodsIssueRepository;
        this.completeGoodsIssueUseCase = completeGoodsIssueUseCase;
        this.ensureOpenPeriodForDateUseCase = ensureOpenPeriodForDateUseCase;
    }

    @Override
    public PurchaseReturn execute(Long id) {
        PurchaseReturn purchaseReturn = purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.not-found"));

        purchaseReturn.validateConfirmation();
        ensureOpenPeriodForDateUseCase.execute(purchaseReturn.getReturnDate());
        if (sourcePort.hasCompletedGoodsIssue(id)) {
            throw new DomainException("msg.error.purchase-return.confirm.goods-issue-exists");
        }

        GoodsIssue draft = resolverRegistry.getResolver(GoodsIssueReferenceType.PURCHASE_RETURN).resolve(id);
        GoodsIssue goodsIssue = GoodsIssue.createNew(
                sequenceGeneratorService.generate("GOODS_ISSUE"),
                purchaseReturn.getReturnDate(),
                draft.getReferenceType(),
                draft.getReferenceId(),
                draft.getReferenceCode(),
                draft.getPartyId(),
                draft.getPartyType(),
                draft.getFacilityId(),
                draft.getCurrencyId(),
                draft.getExchangeRate(),
                draft.getLines()
        );
        GoodsIssue savedGoodsIssue = goodsIssueRepository.save(goodsIssue);
        completeGoodsIssueUseCase.execute(savedGoodsIssue.getId());

        purchaseReturn.confirm(savedGoodsIssue.getId());
        return purchaseReturnRepository.save(purchaseReturn);
    }
}
