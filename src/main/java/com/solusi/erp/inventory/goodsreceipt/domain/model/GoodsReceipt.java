package com.solusi.erp.inventory.goodsreceipt.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class GoodsReceipt {

    private final AuditMetadata metadata;
    private final String code;
    private LocalDate receiptDate;
    private final GoodsReceiptReferenceType referenceType;
    private final Long referenceId;
    private final Long supplierId;
    private final Long facilityId;
    private final Long currencyId;
    private final BigDecimal exchangeRate;
    private GoodsReceiptStatus status;
    private String note;
    private List<GoodsReceiptLine> lines;

    public GoodsReceipt(AuditMetadata metadata, String code, LocalDate receiptDate,
                        GoodsReceiptReferenceType referenceType, Long referenceId, Long supplierId,
                        Long facilityId, Long currencyId, BigDecimal exchangeRate, GoodsReceiptStatus status,
                        String note, List<GoodsReceiptLine> lines) {
        this.metadata = metadata;
        this.code = code;
        this.receiptDate = receiptDate;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.supplierId = supplierId;
        this.facilityId = facilityId;
        this.currencyId = currencyId;
        this.exchangeRate = exchangeRate;
        this.status = status;
        this.note = note;
        this.lines = copyLines(lines);
    }

    public GoodsReceipt(AuditMetadata metadata, String code, LocalDate receiptDate, Long poId, Long supplierId,
                        Long facilityId, Long currencyId, BigDecimal exchangeRate, GoodsReceiptStatus status,
                        String note, List<GoodsReceiptLine> lines) {
        this(metadata, code, receiptDate, GoodsReceiptReferenceType.PURCHASE_ORDER, poId, supplierId,
                facilityId, currencyId, exchangeRate, status, note, lines);
    }

    public static GoodsReceipt createNew(String code, LocalDate receiptDate,
                                         GoodsReceiptReferenceType referenceType, Long referenceId, Long supplierId,
                                         Long facilityId, Long currencyId, BigDecimal exchangeRate,
                                         List<GoodsReceiptLine> lines) {
        return new GoodsReceipt(
                AuditMetadata.empty(), code, receiptDate, referenceType, referenceId, supplierId, facilityId,
                currencyId, exchangeRate, GoodsReceiptStatus.DRAFT, null, lines
        );
    }

    public static GoodsReceipt createNew(String code, LocalDate receiptDate, Long poId, Long supplierId,
                                         Long facilityId, Long currencyId, BigDecimal exchangeRate,
                                         List<GoodsReceiptLine> lines) {
        return createNew(code, receiptDate, GoodsReceiptReferenceType.PURCHASE_ORDER, poId, supplierId,
                facilityId, currencyId, exchangeRate, lines);
    }

    public void update(LocalDate receiptDate, String note, List<GoodsReceiptLine> lines) {
        if (status == GoodsReceiptStatus.COMPLETED) {
            throw new DomainException("msg.error.gr.completed.immutable");
        }
        this.receiptDate = receiptDate;
        this.note = note;
        this.lines = copyLines(lines);
    }

    public void complete() {
        if (status == GoodsReceiptStatus.COMPLETED) {
            throw new DomainException("msg.error.gr.completed.immutable");
        }
        boolean hasPositiveLine = lines.stream().anyMatch(GoodsReceiptLine::hasReceiptQuantity);
        if (!hasPositiveLine) {
            throw new DomainException("msg.error.gr.complete.no.lines");
        }
        this.status = GoodsReceiptStatus.COMPLETED;
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

    public LocalDate getReceiptDate() {
        return receiptDate;
    }

    public GoodsReceiptReferenceType getReferenceType() {
        return referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public Long getPoId() {
        return referenceType == GoodsReceiptReferenceType.PURCHASE_ORDER ? referenceId : null;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public GoodsReceiptStatus getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public List<GoodsReceiptLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    private static List<GoodsReceiptLine> copyLines(List<GoodsReceiptLine> lines) {
        return lines == null ? List.of() : List.copyOf(lines);
    }
}
