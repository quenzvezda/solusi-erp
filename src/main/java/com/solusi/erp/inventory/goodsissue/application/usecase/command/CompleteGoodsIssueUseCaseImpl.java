package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import com.solusi.erp.accounting.journal.application.usecase.command.JournalPostingCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueLine;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.stock.application.dto.StockMovementPayload;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;
import com.solusi.erp.inventory.stock.domain.port.InventoryReservationService;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.uomconversion.domain.port.UomConversionService;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CompleteGoodsIssueUseCaseImpl implements CompleteGoodsIssueUseCase {

    private final GoodsIssueRepository goodsIssueRepository;
    private final EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;
    private final StockService stockService;
    private final UomConversionService uomConversionService;
    private final PostJournalForEventUseCase postJournalForEventUseCase;
    private final InventoryReservationService reservationService;

    public CompleteGoodsIssueUseCaseImpl(GoodsIssueRepository goodsIssueRepository,
                                         EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase,
                                         StockService stockService,
                                         UomConversionService uomConversionService,
                                         PostJournalForEventUseCase postJournalForEventUseCase,
                                         InventoryReservationService reservationService) {
        this.goodsIssueRepository = goodsIssueRepository;
        this.ensureOpenPeriodForDateUseCase = ensureOpenPeriodForDateUseCase;
        this.stockService = stockService;
        this.uomConversionService = uomConversionService;
        this.postJournalForEventUseCase = postJournalForEventUseCase;
        this.reservationService = reservationService;
    }

    @Override
    public void execute(Long id) {
        GoodsIssue issue = goodsIssueRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.gi.notfound"));
        if (issue.getStatus() == GoodsIssueStatus.COMPLETED) {
            throw new DomainException("msg.error.gi.completed.immutable");
        }
        if (issue.getStatus() == GoodsIssueStatus.CANCELLED) {
            throw new DomainException("msg.error.gi.cancelled.immutable");
        }
        ensureOpenPeriodForDateUseCase.execute(issue.getIssueDate());

        List<GoodsIssueLine> snapshottedLines = snapshotAmounts(issue.getLines());
        issue.update(issue.getIssueDate(), issue.getNote(), snapshottedLines);
        if (issue.getLines().stream().noneMatch(GoodsIssueLine::hasIssueQuantity)) {
            throw new DomainException("msg.error.gi.complete.no.lines");
        }
        if (isPurchaseReturn(issue)) {
            reservationService.assertActiveCoverage(
                    ReservationOwnerType.PURCHASE_RETURN,
                    issue.getReferenceId(),
                    GoodsIssueReservationRequests.from(issue.getLines())
            );
        }

        for (GoodsIssueLine line : issue.getLines()) {
            if (!line.hasIssueQuantity()) {
                continue;
            }
            if (Boolean.TRUE.equals(line.getSerialized())) {
                processSerializedLine(issue, line);
            } else {
                stockService.adjust(buildPayload(issue, line, line.getQuantityIssued(), line.getSerialNumber()));
            }
        }

        BigDecimal inventoryTotal = inventoryTotal(issue.getLines());
        postJournalForEventUseCase.execute(journalForIssue(issue, inventoryTotal));

        if (isPurchaseReturn(issue)) {
            reservationService.consume(ReservationOwnerType.PURCHASE_RETURN, issue.getReferenceId());
        }
        issue.complete();
        goodsIssueRepository.save(issue);
    }

    private List<GoodsIssueLine> snapshotAmounts(List<GoodsIssueLine> lines) {
        return lines.stream().map(line -> {
            BigDecimal baseQuantity = resolveBaseQuantity(line);
            BigDecimal unitCost = line.getUnitCost() != null ? line.getUnitCost() : BigDecimal.ZERO;
            BigDecimal inventoryAmount = baseQuantity.multiply(unitCost).setScale(4, RoundingMode.HALF_UP);
            BigDecimal taxBaseAmount = line.getTaxBaseAmount() != null ? line.getTaxBaseAmount() : BigDecimal.ZERO;
            BigDecimal taxAmount = line.getTaxAmount() != null ? line.getTaxAmount() : BigDecimal.ZERO;
            BigDecimal clearingAmount = inventoryAmount.add(taxAmount).setScale(4, RoundingMode.HALF_UP);
            return GoodsIssueLine.reconstitute(
                    line.getMetadata(),
                    line.getReferenceLineId(),
                    line.getProductId(),
                    line.getSerialized(),
                    line.getQuantityIssued(),
                    line.getUomId(),
                    baseQuantity,
                    line.getFacilityId(),
                    line.getGridId(),
                    line.getContainerId(),
                    line.getSerialNumber(),
                    unitCost,
                    inventoryAmount,
                    taxBaseAmount,
                    taxAmount,
                    clearingAmount,
                    line.getValuationRefType(),
                    line.getValuationRefId(),
                    line.getValuationRefLineId()
            );
        }).toList();
    }

    private BigDecimal resolveBaseQuantity(GoodsIssueLine line) {
        if (line.getQuantityIssued() == null) {
            return BigDecimal.ZERO;
        }
        if (line.getUomId() == null) {
            return line.getBaseQuantity() != null ? line.getBaseQuantity() : line.getQuantityIssued();
        }
        return uomConversionService.convertToBaseUom(line.getProductId(), line.getUomId(), line.getQuantityIssued());
    }

    private void processSerializedLine(GoodsIssue issue, GoodsIssueLine line) {
        int totalUnits = resolveSerializedUnitCount(line);
        List<String> serialNumbers = parseProvidedSerialNumbers(line.getSerialNumber());
        if (serialNumbers.size() != totalUnits) {
            throw new DomainException("msg.error.gi.serial.quantity.whole");
        }
        for (String serialNumber : serialNumbers) {
            stockService.adjust(buildPayload(issue, line, BigDecimal.ONE, serialNumber));
        }
    }

    private int resolveSerializedUnitCount(GoodsIssueLine line) {
        BigDecimal baseQuantity = line.getBaseQuantity();
        if (baseQuantity == null || baseQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.gi.serial.quantity.whole");
        }
        try {
            return baseQuantity.abs().intValueExact();
        } catch (ArithmeticException ex) {
            throw new DomainException("msg.error.gi.serial.quantity.whole");
        }
    }

    private List<String> parseProvidedSerialNumbers(String serialNumber) {
        if (!StringUtils.hasText(serialNumber)) {
            return List.of();
        }
        return Arrays.stream(serialNumber.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private StockMovementPayload buildPayload(GoodsIssue issue,
                                              GoodsIssueLine line,
                                              BigDecimal quantity,
                                              String serialNumber) {
        return StockMovementPayload.builder()
                .productId(line.getProductId())
                .containerId(line.getContainerId())
                .serialNumber(serialNumber)
                .quantity(quantity)
                .uomId(line.getUomId())
                .movementType(isPurchaseReturn(issue) ? MovementType.ISSUE_RESERVED : MovementType.ISSUE)
                .referenceType(ReferenceType.GOODS_ISSUE)
                .referenceId(issue.getId())
                .referenceCode(issue.getCode())
                .valuationReferenceType(parseReferenceType(line.getValuationRefType()))
                .valuationReferenceId(line.getValuationRefId())
                .valuationReferenceLineId(line.getValuationRefLineId())
                .currencyId(issue.getCurrencyId())
                .exchangeRate(issue.getExchangeRate())
                .netPrice(line.getUnitCost())
                .transactionDate(issue.getIssueDate().atStartOfDay())
                .build();
    }

    private boolean isPurchaseReturn(GoodsIssue issue) {
        return issue.getReferenceType() == GoodsIssueReferenceType.PURCHASE_RETURN;
    }

    private ReferenceType parseReferenceType(String value) {
        return StringUtils.hasText(value) ? ReferenceType.valueOf(value) : null;
    }

    static JournalPostingCommand goodsIssueJournal(GoodsIssue issue,
                                                   BigDecimal cogsAmount,
                                                   BigDecimal inventoryAmount) {
        return new JournalPostingCommand(
                SchemaEventType.GOODS_ISSUE,
                "GOODS_ISSUE",
                issue.getId(),
                issue.getCode(),
                issue.getIssueDate(),
                "Auto journal for goods issue " + issue.getCode(),
                Map.of(
                        JournalVariable.GI_COGS_AMT, cogsAmount,
                        JournalVariable.GI_INVENTORY_AMT, inventoryAmount
                )
        );
    }

    static JournalPostingCommand journalForIssue(GoodsIssue issue, BigDecimal inventoryAmount) {
        if (isPurchaseReturnSource(issue)) {
            return purchaseReturnJournal(issue, inventoryAmount);
        }
        return goodsIssueJournal(issue, inventoryAmount, inventoryAmount);
    }

    static JournalPostingCommand purchaseReturnJournal(GoodsIssue issue, BigDecimal inventoryAmount) {
        return new JournalPostingCommand(
                SchemaEventType.PURCHASE_RETURN,
                "PURCHASE_RETURN",
                issue.getReferenceId(),
                issue.getReferenceCode(),
                issue.getIssueDate(),
                "Auto journal for purchase return " + issue.getReferenceCode(),
                Map.of(
                        JournalVariable.PR_GRIR_CLEARING_AMT, inventoryAmount,
                        JournalVariable.PR_INVENTORY_AMT, inventoryAmount
                )
        );
    }

    private static boolean isPurchaseReturnSource(GoodsIssue issue) {
        return issue.getReferenceType() == GoodsIssueReferenceType.PURCHASE_RETURN;
    }

    static BigDecimal inventoryTotal(List<GoodsIssueLine> lines) {
        return lines.stream()
                .map(GoodsIssueLine::getInventoryAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
