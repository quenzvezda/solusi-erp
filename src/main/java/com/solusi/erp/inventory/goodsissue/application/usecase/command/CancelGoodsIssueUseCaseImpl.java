package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueLine;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.stock.application.dto.StockMovementPayload;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.port.StockService;

import java.math.BigDecimal;

public class CancelGoodsIssueUseCaseImpl implements CancelGoodsIssueUseCase {

    private final GoodsIssueRepository goodsIssueRepository;
    private final EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;
    private final StockService stockService;
    private final PostJournalForEventUseCase postJournalForEventUseCase;
    private final GoodsIssueInUseChecker inUseChecker;

    public CancelGoodsIssueUseCaseImpl(GoodsIssueRepository goodsIssueRepository,
                                       EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase,
                                       StockService stockService,
                                       PostJournalForEventUseCase postJournalForEventUseCase,
                                       GoodsIssueInUseChecker inUseChecker) {
        this.goodsIssueRepository = goodsIssueRepository;
        this.ensureOpenPeriodForDateUseCase = ensureOpenPeriodForDateUseCase;
        this.stockService = stockService;
        this.postJournalForEventUseCase = postJournalForEventUseCase;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public void execute(Long id, String reason) {
        GoodsIssue issue = goodsIssueRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.gi.notfound"));
        if (issue.getStatus() != GoodsIssueStatus.COMPLETED) {
            throw new DomainException("msg.error.gi.cancel.only.completed");
        }
        ensureOpenPeriodForDateUseCase.execute(issue.getIssueDate());
        inUseChecker.assertNotInUse(issue);

        for (GoodsIssueLine line : issue.getLines()) {
            if (!line.hasIssueQuantity()) {
                continue;
            }
            stockService.adjust(buildReversalPayload(issue, line));
        }

        BigDecimal inventoryTotal = CompleteGoodsIssueUseCaseImpl.inventoryTotal(issue.getLines()).negate();
        postJournalForEventUseCase.execute(CompleteGoodsIssueUseCaseImpl.goodsIssueJournal(
                issue, inventoryTotal, inventoryTotal));

        issue.cancel();
        goodsIssueRepository.save(issue);
    }

    private StockMovementPayload buildReversalPayload(GoodsIssue issue, GoodsIssueLine line) {
        return StockMovementPayload.builder()
                .productId(line.getProductId())
                .containerId(line.getContainerId())
                .serialNumber(line.getSerialNumber())
                .quantity(line.getQuantityIssued())
                .uomId(line.getUomId())
                .movementType(MovementType.RECEIPT)
                .referenceType(ReferenceType.GOODS_ISSUE)
                .referenceId(issue.getId())
                .referenceCode(issue.getCode())
                .valuationReferenceType(ReferenceType.GOODS_ISSUE)
                .valuationReferenceId(issue.getId())
                .valuationReferenceLineId(line.getId())
                .currencyId(issue.getCurrencyId())
                .exchangeRate(issue.getExchangeRate())
                .netPrice(line.getUnitCost())
                .transactionDate(issue.getIssueDate().atStartOfDay())
                .build();
    }
}
