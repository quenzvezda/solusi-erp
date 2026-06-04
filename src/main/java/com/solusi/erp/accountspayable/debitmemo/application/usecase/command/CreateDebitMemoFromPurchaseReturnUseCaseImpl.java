package com.solusi.erp.accountspayable.debitmemo.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoLine;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;

import java.math.BigDecimal;
import java.util.List;

public class CreateDebitMemoFromPurchaseReturnUseCaseImpl implements CreateDebitMemoFromPurchaseReturnUseCase {

    private static final String MODULE_CODE = "DEBIT_MEMO";

    private final DebitMemoRepository debitMemoRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreateDebitMemoFromPurchaseReturnUseCaseImpl(DebitMemoRepository debitMemoRepository,
                                                        SequenceGeneratorService sequenceGeneratorService) {
        this.debitMemoRepository = debitMemoRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public DebitMemo execute(DebitMemoPurchaseReturnSource source) {
        if (source == null || source.purchaseReturnId() == null) {
            throw new DomainException("msg.error.debit-memo.purchase-return-required");
        }
        return debitMemoRepository.findByPurchaseReturnId(source.purchaseReturnId())
                .orElseGet(() -> create(source));
    }

    private DebitMemo create(DebitMemoPurchaseReturnSource source) {
        DebitMemo debitMemo = DebitMemo.createNew(
                sequenceGeneratorService.generate(MODULE_CODE),
                source.purchaseReturnId(),
                source.purchaseReturnCode(),
                source.vendorId(),
                source.currencyId(),
                source.returnDate(),
                toLines(source.lines())
        );
        return debitMemoRepository.save(debitMemo);
    }

    private List<DebitMemoLine> toLines(List<DebitMemoPurchaseReturnLineSource> lines) {
        if (lines == null) {
            return List.of();
        }
        return lines.stream().map(this::toLine).toList();
    }

    private DebitMemoLine toLine(DebitMemoPurchaseReturnLineSource sourceLine) {
        BigDecimal dppAmount = zeroIfNull(sourceLine.dppAmount());
        BigDecimal taxAmount = zeroIfNull(sourceLine.taxAmount());
        return new DebitMemoLine(
                null,
                sourceLine.purchaseReturnLineId(),
                sourceLine.productId(),
                sourceLine.quantity(),
                sourceLine.uomId(),
                dppAmount,
                taxAmount,
                dppAmount,
                taxAmount
        );
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

