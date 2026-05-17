package com.solusi.erp.accountspayable.vendorpayment.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VendorPayment {

    private final AuditMetadata metadata;
    private String code;
    private Long vendorId;
    private Long currencyId;
    private Long bankAccountId;
    private LocalDate paymentDate;
    private BigDecimal exchangeRate;
    private BigDecimal paymentAmount;
    private VendorPaymentStatus status;
    private String reference;
    private String notes;
    private List<VendorPaymentLine> lines;

    public VendorPayment(AuditMetadata metadata, String code, Long vendorId, Long currencyId,
                         Long bankAccountId, LocalDate paymentDate, BigDecimal exchangeRate,
                         BigDecimal paymentAmount, VendorPaymentStatus status,
                         String reference, String notes, List<VendorPaymentLine> lines) {
        this.metadata = metadata;
        this.code = code;
        this.vendorId = vendorId;
        this.currencyId = currencyId;
        this.bankAccountId = bankAccountId;
        this.paymentDate = paymentDate;
        this.exchangeRate = exchangeRate;
        this.paymentAmount = paymentAmount;
        this.status = status;
        this.reference = reference;
        this.notes = notes;
        this.lines = copyLines(lines);
    }

    public static VendorPayment createNew(String code, Long vendorId, Long currencyId,
                                           Long bankAccountId, LocalDate paymentDate,
                                           BigDecimal exchangeRate, BigDecimal paymentAmount,
                                           String reference, String notes,
                                           List<VendorPaymentLine> lines) {
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.err.vp.amount.positive");
        }
        if (lines == null || lines.isEmpty()) {
            throw new DomainException("msg.err.vp.lines.required");
        }
        return new VendorPayment(AuditMetadata.empty(), code, vendorId, currencyId,
                bankAccountId, paymentDate, exchangeRate, paymentAmount,
                VendorPaymentStatus.DRAFT, reference, notes, lines);
    }

    public void update(Long vendorId, Long currencyId, Long bankAccountId,
                       LocalDate paymentDate, BigDecimal exchangeRate, BigDecimal paymentAmount,
                       String reference, String notes, List<VendorPaymentLine> lines) {
        if (this.status != VendorPaymentStatus.DRAFT) {
            throw new DomainException("msg.err.vp.edit.only.draft");
        }
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.err.vp.amount.positive");
        }
        if (lines == null || lines.isEmpty()) {
            throw new DomainException("msg.err.vp.lines.required");
        }
        this.vendorId = vendorId;
        this.currencyId = currencyId;
        this.bankAccountId = bankAccountId;
        this.paymentDate = paymentDate;
        this.exchangeRate = exchangeRate;
        this.paymentAmount = paymentAmount;
        this.reference = reference;
        this.notes = notes;
        this.lines = copyLines(lines);
    }

    public void confirm() {
        if (this.status != VendorPaymentStatus.DRAFT) {
            throw new DomainException("msg.err.vp.confirm.only.draft");
        }
        if (lines == null || lines.isEmpty()) {
            throw new DomainException("msg.err.vp.lines.required");
        }
        BigDecimal totalPaid = lines.stream()
                .map(VendorPaymentLine::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalPaid.compareTo(paymentAmount) != 0) {
            throw new DomainException("msg.err.vp.amount.mismatch");
        }
        this.status = VendorPaymentStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status != VendorPaymentStatus.DRAFT) {
            throw new DomainException("msg.err.vp.cancel.only.draft");
        }
        this.status = VendorPaymentStatus.CANCELLED;
    }

    private List<VendorPaymentLine> copyLines(List<VendorPaymentLine> lines) {
        if (lines == null) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(lines));
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public Long getVendorId() { return vendorId; }
    public Long getCurrencyId() { return currencyId; }
    public Long getBankAccountId() { return bankAccountId; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public VendorPaymentStatus getStatus() { return status; }
    public String getReference() { return reference; }
    public String getNotes() { return notes; }
    public List<VendorPaymentLine> getLines() { return lines; }
}
