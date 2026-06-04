package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.command.CreateDebitMemoFromPurchaseReturnUseCase;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.command.DebitMemoPurchaseReturnLineSource;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.command.DebitMemoPurchaseReturnSource;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CompleteGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.port.PurchaseReturnGoodsIssueSourcePort;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.service.GoodsIssueSourceResolverRegistry;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnLine;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;

import java.math.BigDecimal;
import java.util.List;

public class ConfirmPurchaseReturnUseCaseImpl implements ConfirmPurchaseReturnUseCase {

    private final PurchaseReturnRepository purchaseReturnRepository;
    private final PurchaseReturnGoodsIssueSourcePort sourcePort;
    private final GoodsIssueSourceResolverRegistry resolverRegistry;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final GoodsIssueRepository goodsIssueRepository;
    private final CompleteGoodsIssueUseCase completeGoodsIssueUseCase;
    private final EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;
    private final CreateDebitMemoFromPurchaseReturnUseCase createDebitMemoFromPurchaseReturnUseCase;

    public ConfirmPurchaseReturnUseCaseImpl(PurchaseReturnRepository purchaseReturnRepository,
                                            PurchaseReturnGoodsIssueSourcePort sourcePort,
                                            GoodsIssueSourceResolverRegistry resolverRegistry,
                                            SequenceGeneratorService sequenceGeneratorService,
                                            GoodsIssueRepository goodsIssueRepository,
                                            CompleteGoodsIssueUseCase completeGoodsIssueUseCase,
                                            EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase,
                                            CreateDebitMemoFromPurchaseReturnUseCase createDebitMemoFromPurchaseReturnUseCase) {
        this.purchaseReturnRepository = purchaseReturnRepository;
        this.sourcePort = sourcePort;
        this.resolverRegistry = resolverRegistry;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.goodsIssueRepository = goodsIssueRepository;
        this.completeGoodsIssueUseCase = completeGoodsIssueUseCase;
        this.ensureOpenPeriodForDateUseCase = ensureOpenPeriodForDateUseCase;
        this.createDebitMemoFromPurchaseReturnUseCase = createDebitMemoFromPurchaseReturnUseCase;
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

        createDebitMemoFromPurchaseReturnUseCase.execute(toDebitMemoSource(purchaseReturn));
        purchaseReturn.confirm(savedGoodsIssue.getId());
        return purchaseReturnRepository.save(purchaseReturn);
    }

    private DebitMemoPurchaseReturnSource toDebitMemoSource(PurchaseReturn purchaseReturn) {
        return new DebitMemoPurchaseReturnSource(
                purchaseReturn.getId(),
                purchaseReturn.getCode(),
                purchaseReturn.getSupplierId(),
                purchaseReturn.getCurrencyId(),
                purchaseReturn.getReturnDate(),
                toDebitMemoLines(purchaseReturn.getLines())
        );
    }

    private List<DebitMemoPurchaseReturnLineSource> toDebitMemoLines(List<PurchaseReturnLine> lines) {
        return lines.stream()
                .map(line -> new DebitMemoPurchaseReturnLineSource(
                        line.getId(),
                        line.getProductId(),
                        line.getQuantity(),
                        line.getUomId(),
                        zeroIfNull(line.getClearingAmount()),
                        zeroIfNull(line.getTaxReversalAmount())
                ))
                .toList();
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
