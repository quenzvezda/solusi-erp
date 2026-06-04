package com.solusi.erp.accountspayable.vendorbill.application.usecase.command;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillGrRef;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillLine;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreateVendorBillUseCaseImpl implements CreateVendorBillUseCase {

    private final VendorBillRepository vendorBillRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreateVendorBillUseCaseImpl(VendorBillRepository vendorBillRepository,
                                       SequenceGeneratorService sequenceGeneratorService) {
        this.vendorBillRepository = vendorBillRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public VendorBill execute(Long vendorId,
                              String vendorInvoiceNumber,
                              LocalDate billDate,
                              LocalDate dueDate,
                              Long currencyId,
                              BigDecimal exchangeRate,
                              String notes,
                              List<Long> grIds,
                              List<VendorBillLineCommand> lines) {
        validateDates(billDate, dueDate);
        validateLines(lines);

        List<VendorBillLine> billLines = toLines(lines);
        VendorBill vendorBill = new VendorBill(
                com.solusi.erp.core.domain.model.AuditMetadata.empty(),
                sequenceGeneratorService.generate("VENDOR-BILL"),
                vendorId,
                vendorInvoiceNumber,
                billDate,
                dueDate,
                currencyId,
                exchangeRate,
                com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillDocumentStatus.DRAFT,
                null,
                sumLineTotals(billLines),
                sumTaxAmounts(billLines),
                sumGrossAmounts(billLines),
                notes,
                toGrRefs(grIds),
                billLines
        );

        return vendorBillRepository.save(vendorBill);
    }

    private void validateDates(LocalDate billDate, LocalDate dueDate) {
        if (billDate != null && dueDate != null && dueDate.isBefore(billDate)) {
            throw new DomainException("msg.error.vb.due.before.bill");
        }
    }

    private void validateLines(List<VendorBillLineCommand> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new DomainException("msg.err.vb.lines.required");
        }
    }

    private List<VendorBillGrRef> toGrRefs(List<Long> grIds) {
        if (grIds == null) {
            return List.of();
        }
        return grIds.stream()
                .map(grId -> new VendorBillGrRef(null, grId))
                .toList();
    }

    private List<VendorBillLine> toLines(List<VendorBillLineCommand> lines) {
        if (lines == null) {
            return List.of();
        }
        return lines.stream()
                .map(this::toLine)
                .toList();
    }

    private VendorBillLine toLine(VendorBillLineCommand line) {
        return new VendorBillLine(
                line.id(),
                line.grLineId(),
                line.productId(),
                line.productName(),
                line.description(),
                line.qtyBilled(),
                line.uomId(),
                line.uomName(),
                line.unitPrice(),
                line.inventoryAmount(),
                line.taxAmount(),
                line.inventoryAmount() == null ? BigDecimal.ZERO : line.inventoryAmount()
        );
    }

    private BigDecimal sumLineTotals(List<VendorBillLine> lines) {
        return lines.stream()
                .map(VendorBillLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumTaxAmounts(List<VendorBillLine> lines) {
        return lines.stream()
                .map(VendorBillLine::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumGrossAmounts(List<VendorBillLine> lines) {
        return sumLineTotals(lines).add(sumTaxAmounts(lines));
    }
}
