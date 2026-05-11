package com.solusi.erp.accountspayable.vendorbill.application.usecase.command;

import com.solusi.erp.accounting.journal.application.usecase.command.JournalPostingCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillLine;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrQueryPort;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfirmVendorBillUseCaseImpl implements ConfirmVendorBillUseCase {

    private final VendorBillRepository vendorBillRepository;
    private final BillableGrQueryPort billableGrQueryPort;
    private final EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;
    private final PostJournalForEventUseCase postJournalForEventUseCase;

    public ConfirmVendorBillUseCaseImpl(VendorBillRepository vendorBillRepository,
                                        BillableGrQueryPort billableGrQueryPort,
                                        EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase,
                                        PostJournalForEventUseCase postJournalForEventUseCase) {
        this.vendorBillRepository = vendorBillRepository;
        this.billableGrQueryPort = billableGrQueryPort;
        this.ensureOpenPeriodForDateUseCase = ensureOpenPeriodForDateUseCase;
        this.postJournalForEventUseCase = postJournalForEventUseCase;
    }

    @Override
    public void execute(Long id) {
        VendorBill bill = vendorBillRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.err.vb.not.found"));

        ensureOpenPeriodForDateUseCase.execute(bill.getBillDate());

        Map<Long, BigDecimal> currentQtyByGrLine = bill.getLines().stream()
                .collect(Collectors.groupingBy(
                        VendorBillLine::getGrLineId,
                        Collectors.reducing(BigDecimal.ZERO, VendorBillLine::getQtyBilled, BigDecimal::add)
                ));
        Map<Long, GrLineContext> contexts = buildGrLineContexts(bill.getId(), currentQtyByGrLine);
        Map<Long, BigDecimal> accumulatedQtyByGrLine = new HashMap<>();
        Map<Long, BigDecimal> accumulatedTotalByGrLine = new HashMap<>();

        List<VendorBillLine> confirmedLines = bill.getLines().stream()
                .map(line -> recalculateLine(line, contexts.get(line.getGrLineId()),
                        accumulatedQtyByGrLine, accumulatedTotalByGrLine))
                .toList();
        BigDecimal total = confirmedLines.stream()
                .map(VendorBillLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        VendorBill confirmedBill = new VendorBill(
                bill.getMetadata(),
                bill.getCode(),
                bill.getVendorId(),
                bill.getVendorInvoiceNumber(),
                bill.getBillDate(),
                bill.getDueDate(),
                bill.getCurrencyId(),
                VendorBillStatus.DRAFT,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                bill.getNotes(),
                bill.getGrRefs(),
                confirmedLines
        );
        confirmedBill.confirm(total, BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP), total);

        postJournalForEventUseCase.execute(new JournalPostingCommand(
                SchemaEventType.VENDOR_BILL,
                "VENDOR_BILL",
                confirmedBill.getId(),
                confirmedBill.getCode(),
                confirmedBill.getBillDate(),
                "Auto journal for vendor bill " + confirmedBill.getCode(),
                Map.of(
                        JournalVariable.VB_GRIR_CLEARING_AMT, total,
                        JournalVariable.VB_TAX_AMT, BigDecimal.ZERO,
                        JournalVariable.VB_AP_TOTAL, total
                )
        ));

        vendorBillRepository.save(confirmedBill);
    }

    private Map<Long, GrLineContext> buildGrLineContexts(Long billId, Map<Long, BigDecimal> currentQtyByGrLine) {
        Map<Long, GrLineContext> contexts = new HashMap<>();
        currentQtyByGrLine.forEach((grLineId, currentQty) -> {
            BillableGrQueryPort.GrLineData grLineData = billableGrQueryPort.getGrLineData(grLineId);
            BigDecimal alreadyBilled = billableGrQueryPort.sumConfirmedBilledQty(grLineId, billId);
            BigDecimal outstanding = grLineData.quantityReceived().subtract(alreadyBilled);
            if (currentQty.compareTo(outstanding) > 0) {
                throw new DomainException("msg.err.vb.qty.exceed.outstanding");
            }
            BigDecimal confirmedTotal = billableGrQueryPort.sumConfirmedLineTotals(grLineId, billId);
            contexts.put(grLineId, new GrLineContext(grLineData, outstanding, currentQty, confirmedTotal));
        });
        return contexts;
    }

    private VendorBillLine recalculateLine(VendorBillLine line,
                                           GrLineContext context,
                                           Map<Long, BigDecimal> accumulatedQtyByGrLine,
                                           Map<Long, BigDecimal> accumulatedTotalByGrLine) {
        Long grLineId = line.getGrLineId();
        BigDecimal accumulatedQty = accumulatedQtyByGrLine.merge(grLineId, line.getQtyBilled(), BigDecimal::add);
        BigDecimal accumulatedTotal = accumulatedTotalByGrLine.getOrDefault(grLineId, BigDecimal.ZERO);

        BigDecimal lineTotal = isLastBillLine(context, accumulatedQty)
                ? context.grLineData().grIrAmount()
                .subtract(context.confirmedLineTotal())
                .subtract(accumulatedTotal)
                .setScale(4, RoundingMode.HALF_UP)
                : line.getQtyBilled()
                .multiply(context.grLineData().grIrAmount())
                .divide(context.grLineData().quantityReceived(), 4, RoundingMode.HALF_UP);
        accumulatedTotalByGrLine.merge(grLineId, lineTotal, BigDecimal::add);

        return new VendorBillLine(
                line.getId(),
                line.getGrLineId(),
                line.getProductId(),
                line.getProductName(),
                line.getDescription(),
                line.getQtyBilled(),
                line.getUomId(),
                line.getUomName(),
                line.getUnitPrice(),
                line.getInventoryAmount(),
                line.getTaxAmount(),
                lineTotal
        );
    }

    private boolean isLastBillLine(GrLineContext context, BigDecimal accumulatedQty) {
        return accumulatedQty.compareTo(context.currentQty()) == 0
                && context.currentQty().compareTo(context.outstandingQty()) == 0;
    }

    private record GrLineContext(BillableGrQueryPort.GrLineData grLineData,
                                 BigDecimal outstandingQty,
                                 BigDecimal currentQty,
                                 BigDecimal confirmedLineTotal) {
    }
}
