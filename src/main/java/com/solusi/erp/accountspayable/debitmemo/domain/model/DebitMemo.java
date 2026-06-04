package com.solusi.erp.accountspayable.debitmemo.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class DebitMemo {

    private final AuditMetadata metadata;
    private final String code;
    private final Long purchaseReturnId;
    private final String purchaseReturnCode;
    private final Long vendorId;
    private final Long currencyId;
    private final LocalDate memoDate;
    private final BigDecimal grossAmountOriginal;
    private final BigDecimal dppAmountOriginal;
    private final BigDecimal taxAmountOriginal;
    private final BigDecimal grossAmountBase;
    private final BigDecimal dppAmountBase;
    private final BigDecimal taxAmountBase;
    private DebitMemoSettlementStatus settlementStatus;
    private String supplierMemoNumber;
    private LocalDate supplierMemoDate;
    private String taxDocumentNumber;
    private LocalDate taxDocumentDate;
    private String notes;
    private final List<DebitMemoLine> lines;

    private DebitMemo(AuditMetadata metadata,
                      String code,
                      Long purchaseReturnId,
                      String purchaseReturnCode,
                      Long vendorId,
                      Long currencyId,
                      LocalDate memoDate,
                      DebitMemoSettlementStatus settlementStatus,
                      String supplierMemoNumber,
                      LocalDate supplierMemoDate,
                      String taxDocumentNumber,
                      LocalDate taxDocumentDate,
                      String notes,
                      List<DebitMemoLine> lines) {
        validateHeader(code, purchaseReturnId, purchaseReturnCode, vendorId, currencyId, memoDate, lines);
        this.metadata = metadata == null ? AuditMetadata.empty() : metadata;
        this.code = code;
        this.purchaseReturnId = purchaseReturnId;
        this.purchaseReturnCode = purchaseReturnCode;
        this.vendorId = vendorId;
        this.currencyId = currencyId;
        this.memoDate = memoDate;
        this.lines = copyLines(lines);
        this.dppAmountOriginal = sumOriginalDpp(this.lines);
        this.taxAmountOriginal = sumOriginalTax(this.lines);
        this.grossAmountOriginal = this.dppAmountOriginal.add(this.taxAmountOriginal);
        this.dppAmountBase = sumBaseDpp(this.lines);
        this.taxAmountBase = sumBaseTax(this.lines);
        this.grossAmountBase = this.dppAmountBase.add(this.taxAmountBase);
        if (this.grossAmountOriginal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.debit-memo.gross-positive");
        }
        this.settlementStatus = settlementStatus == null ? DebitMemoSettlementStatus.OPEN : settlementStatus;
        this.supplierMemoNumber = supplierMemoNumber;
        this.supplierMemoDate = supplierMemoDate;
        this.taxDocumentNumber = taxDocumentNumber;
        this.taxDocumentDate = taxDocumentDate;
        this.notes = notes;
    }

    public static DebitMemo createNew(String code,
                                      Long purchaseReturnId,
                                      String purchaseReturnCode,
                                      Long vendorId,
                                      Long currencyId,
                                      LocalDate memoDate,
                                      List<DebitMemoLine> lines) {
        return new DebitMemo(AuditMetadata.empty(), code, purchaseReturnId, purchaseReturnCode, vendorId,
                currencyId, memoDate, DebitMemoSettlementStatus.OPEN, null, null, null, null, null, lines);
    }

    public static DebitMemo reconstitute(AuditMetadata metadata,
                                         String code,
                                         Long purchaseReturnId,
                                         String purchaseReturnCode,
                                         Long vendorId,
                                         Long currencyId,
                                         LocalDate memoDate,
                                         DebitMemoSettlementStatus settlementStatus,
                                         String supplierMemoNumber,
                                         LocalDate supplierMemoDate,
                                         String taxDocumentNumber,
                                         LocalDate taxDocumentDate,
                                         String notes,
                                         List<DebitMemoLine> lines) {
        return new DebitMemo(metadata, code, purchaseReturnId, purchaseReturnCode, vendorId, currencyId, memoDate,
                settlementStatus, supplierMemoNumber, supplierMemoDate, taxDocumentNumber, taxDocumentDate, notes, lines);
    }

    public void updateMetadata(String supplierMemoNumber,
                               LocalDate supplierMemoDate,
                               String taxDocumentNumber,
                               LocalDate taxDocumentDate,
                               String notes) {
        if (settlementStatus == DebitMemoSettlementStatus.CANCELLED) {
            throw new DomainException("msg.error.debit-memo.metadata.cancelled");
        }
        this.supplierMemoNumber = blankToNull(supplierMemoNumber);
        this.supplierMemoDate = supplierMemoDate;
        this.taxDocumentNumber = blankToNull(taxDocumentNumber);
        this.taxDocumentDate = taxDocumentDate;
        this.notes = notes;
    }

    public void markPartiallySettled() {
        if (settlementStatus != DebitMemoSettlementStatus.OPEN) {
            throw new DomainException("msg.error.debit-memo.invalid-settlement-transition");
        }
        settlementStatus = DebitMemoSettlementStatus.PARTIALLY_SETTLED;
    }

    public void markSettled() {
        if (settlementStatus != DebitMemoSettlementStatus.OPEN
                && settlementStatus != DebitMemoSettlementStatus.PARTIALLY_SETTLED) {
            throw new DomainException("msg.error.debit-memo.invalid-settlement-transition");
        }
        settlementStatus = DebitMemoSettlementStatus.SETTLED;
    }

    public void cancel() {
        if (settlementStatus != DebitMemoSettlementStatus.OPEN) {
            throw new DomainException("msg.error.debit-memo.cancel.only-open");
        }
        settlementStatus = DebitMemoSettlementStatus.CANCELLED;
    }

    private static void validateHeader(String code,
                                       Long purchaseReturnId,
                                       String purchaseReturnCode,
                                       Long vendorId,
                                       Long currencyId,
                                       LocalDate memoDate,
                                       List<DebitMemoLine> lines) {
        if (isBlank(code)) {
            throw new DomainException("msg.error.debit-memo.code-required");
        }
        if (purchaseReturnId == null || isBlank(purchaseReturnCode)) {
            throw new DomainException("msg.error.debit-memo.purchase-return-required");
        }
        if (vendorId == null || currencyId == null) {
            throw new DomainException("msg.error.debit-memo.vendor-currency-required");
        }
        if (memoDate == null) {
            throw new DomainException("msg.error.debit-memo.memo-date-required");
        }
        if (lines == null || lines.isEmpty()) {
            throw new DomainException("msg.error.debit-memo.lines-required");
        }
    }

    private static List<DebitMemoLine> copyLines(List<DebitMemoLine> lines) {
        return List.copyOf(lines);
    }

    private static BigDecimal sumOriginalDpp(List<DebitMemoLine> lines) {
        return lines.stream().map(DebitMemoLine::getDppAmountOriginal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal sumOriginalTax(List<DebitMemoLine> lines) {
        return lines.stream().map(DebitMemoLine::getTaxAmountOriginal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal sumBaseDpp(List<DebitMemoLine> lines) {
        return lines.stream().map(DebitMemoLine::getDppAmountBase).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal sumBaseTax(List<DebitMemoLine> lines) {
        return lines.stream().map(DebitMemoLine::getTaxAmountBase).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public Long getId() {
        return metadata.id();
    }

    public AuditMetadata getMetadata() {
        return metadata;
    }

    public String getCode() {
        return code;
    }

    public Long getPurchaseReturnId() {
        return purchaseReturnId;
    }

    public String getPurchaseReturnCode() {
        return purchaseReturnCode;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public LocalDate getMemoDate() {
        return memoDate;
    }

    public BigDecimal getGrossAmountOriginal() {
        return grossAmountOriginal;
    }

    public BigDecimal getDppAmountOriginal() {
        return dppAmountOriginal;
    }

    public BigDecimal getTaxAmountOriginal() {
        return taxAmountOriginal;
    }

    public BigDecimal getGrossAmountBase() {
        return grossAmountBase;
    }

    public BigDecimal getDppAmountBase() {
        return dppAmountBase;
    }

    public BigDecimal getTaxAmountBase() {
        return taxAmountBase;
    }

    public DebitMemoSettlementStatus getSettlementStatus() {
        return settlementStatus;
    }

    public String getSupplierMemoNumber() {
        return supplierMemoNumber;
    }

    public LocalDate getSupplierMemoDate() {
        return supplierMemoDate;
    }

    public String getTaxDocumentNumber() {
        return taxDocumentNumber;
    }

    public LocalDate getTaxDocumentDate() {
        return taxDocumentDate;
    }

    public String getNotes() {
        return notes;
    }

    public List<DebitMemoLine> getLines() {
        return Collections.unmodifiableList(lines);
    }
}

