package com.solusi.erp.accountspayable.vendorbill.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class VendorBill {

    private final AuditMetadata metadata;
    private final String code;
    private final Long vendorId;
    private final String vendorInvoiceNumber;
    private final LocalDate billDate;
    private final LocalDate dueDate;
    private final Long currencyId;
    private final BigDecimal exchangeRate;
    private VendorBillDocumentStatus documentStatus;
    private VendorBillSettlementStatus settlementStatus;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private final String notes;
    private final List<VendorBillGrRef> grRefs;
    private final List<VendorBillLine> lines;

    public VendorBill(AuditMetadata metadata, String code, Long vendorId, String vendorInvoiceNumber,
                      LocalDate billDate, LocalDate dueDate, Long currencyId, BigDecimal exchangeRate,
                      VendorBillDocumentStatus documentStatus, VendorBillSettlementStatus settlementStatus,
                      BigDecimal subtotal, BigDecimal taxAmount, BigDecimal totalAmount, String notes,
                      List<VendorBillGrRef> grRefs, List<VendorBillLine> lines) {
        this.metadata = metadata;
        this.code = code;
        this.vendorId = vendorId;
        this.vendorInvoiceNumber = vendorInvoiceNumber;
        this.billDate = billDate;
        this.dueDate = dueDate;
        this.currencyId = currencyId;
        this.exchangeRate = exchangeRate == null ? BigDecimal.ONE : exchangeRate;
        this.documentStatus = documentStatus;
        this.settlementStatus = settlementStatus;
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.notes = notes;
        this.grRefs = copyGrRefs(grRefs);
        this.lines = copyLines(lines);
    }

    public static VendorBill createNew(String code, Long vendorId, String vendorInvoiceNumber,
                                       LocalDate billDate, LocalDate dueDate, Long currencyId,
                                       BigDecimal exchangeRate, String notes,
                                       List<VendorBillGrRef> grRefs, List<VendorBillLine> lines) {
        return new VendorBill(
                AuditMetadata.empty(), code, vendorId, vendorInvoiceNumber, billDate, dueDate, currencyId,
                exchangeRate, VendorBillDocumentStatus.DRAFT, null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, notes,
                grRefs, lines
        );
    }

    public void confirm(BigDecimal subtotal, BigDecimal taxAmount, BigDecimal totalAmount) {
        if (documentStatus != VendorBillDocumentStatus.DRAFT) {
            throw new DomainException("msg.error.vb.invalid.status");
        }
        if (lines == null || lines.isEmpty()) {
            throw new DomainException("msg.error.vb.lines.required");
        }
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.documentStatus = VendorBillDocumentStatus.CONFIRMED;
        this.settlementStatus = VendorBillSettlementStatus.OPEN;
    }

    public void cancel() {
        if (documentStatus != VendorBillDocumentStatus.DRAFT) {
            throw new DomainException("msg.error.vb.invalid.status");
        }
        this.documentStatus = VendorBillDocumentStatus.CANCELLED;
        this.settlementStatus = null;
    }

    public Long getId() {
        return metadata.id();
    }

    public String getCode() {
        return code;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public String getVendorInvoiceNumber() {
        return vendorInvoiceNumber;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public VendorBillDocumentStatus getDocumentStatus() {
        return documentStatus;
    }

    public VendorBillSettlementStatus getSettlementStatus() {
        return settlementStatus;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getNotes() {
        return notes;
    }

    public List<VendorBillGrRef> getGrRefs() {
        return Collections.unmodifiableList(grRefs);
    }

    public List<VendorBillLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public AuditMetadata getMetadata() {
        return metadata;
    }

    private static List<VendorBillGrRef> copyGrRefs(List<VendorBillGrRef> grRefs) {
        return grRefs == null ? List.of() : List.copyOf(grRefs);
    }

    private static List<VendorBillLine> copyLines(List<VendorBillLine> lines) {
        return lines == null ? List.of() : List.copyOf(lines);
    }
}
