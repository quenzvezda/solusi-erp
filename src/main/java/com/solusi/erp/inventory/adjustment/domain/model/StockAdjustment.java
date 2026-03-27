package com.solusi.erp.inventory.adjustment.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate Root: StockAdjustment.
 * Pure Java Domain Model — no Spring, no Lombok.
 */
public class StockAdjustment {

    private final AuditMetadata metadata;
    private String code;
    private LocalDate transactionDate;
    private AdjustmentStatus status;
    private String note;
    private Long facilityId;
    private String facilityName;
    private Long currencyId;
    private String currencyAlias;
    private BigDecimal exchangeRate;
    private BigDecimal totalAmountOriginal;
    private BigDecimal totalAmountLocal;
    private List<StockAdjustmentLineItem> lines;

    public StockAdjustment(AuditMetadata metadata, String code, LocalDate transactionDate,
                           AdjustmentStatus status, String note, Long facilityId, String facilityName,
                           Long currencyId, String currencyAlias, BigDecimal exchangeRate,
                           BigDecimal totalAmountOriginal, BigDecimal totalAmountLocal,
                           List<StockAdjustmentLineItem> lines) {
        this.metadata = metadata;
        this.code = code;
        this.transactionDate = transactionDate;
        this.status = status;
        this.note = note;
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.currencyId = currencyId;
        this.currencyAlias = currencyAlias;
        this.exchangeRate = exchangeRate;
        this.totalAmountOriginal = totalAmountOriginal;
        this.totalAmountLocal = totalAmountLocal;
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    public static StockAdjustment createNew(LocalDate transactionDate, String note,
                                            Long facilityId, String facilityName,
                                            Long currencyId, String currencyAlias,
                                            BigDecimal exchangeRate,
                                            List<StockAdjustmentLineItem> lines) {
        StockAdjustment sa = new StockAdjustment(AuditMetadata.empty(), null, transactionDate,
                AdjustmentStatus.DRAFT, note, facilityId, facilityName, currencyId, currencyAlias,
                exchangeRate, BigDecimal.ZERO, BigDecimal.ZERO, lines);
        sa.calculateTotals();
        return sa;
    }

    public void update(LocalDate transactionDate, String note,
                       Long facilityId, String facilityName,
                       Long currencyId, String currencyAlias,
                       BigDecimal exchangeRate,
                       List<StockAdjustmentLineItem> lines) {
        if (this.status == AdjustmentStatus.COMPLETED) {
            throw new RuntimeException("msg.error.stock-adjustment.already-completed");
        }
        this.transactionDate = transactionDate;
        this.note = note;
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.currencyId = currencyId;
        this.currencyAlias = currencyAlias;
        this.exchangeRate = exchangeRate;
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
        calculateTotals();
    }

    public List<StockAdjustmentLineItem> process() {
        if (this.status == AdjustmentStatus.COMPLETED) {
            throw new RuntimeException("msg.error.stock-adjustment.already-completed");
        }
        this.status = AdjustmentStatus.COMPLETED;
        return new ArrayList<>(this.lines);
    }

    private void calculateTotals() {
        BigDecimal rate = (this.exchangeRate != null && this.exchangeRate.compareTo(BigDecimal.ZERO) > 0)
                ? this.exchangeRate : BigDecimal.ONE;
        BigDecimal original = BigDecimal.ZERO;
        for (StockAdjustmentLineItem line : this.lines) {
            if (line.getUnitCost() != null && line.getQuantity() != null) {
                original = original.add(line.getUnitCost().multiply(line.getQuantity()));
            }
        }
        this.totalAmountOriginal = original;
        this.totalAmountLocal = original.multiply(rate);
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public AdjustmentStatus getStatus() { return status; }
    public String getNote() { return note; }
    public Long getFacilityId() { return facilityId; }
    public String getFacilityName() { return facilityName; }
    public Long getCurrencyId() { return currencyId; }
    public String getCurrencyAlias() { return currencyAlias; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public BigDecimal getTotalAmountOriginal() { return totalAmountOriginal; }
    public BigDecimal getTotalAmountLocal() { return totalAmountLocal; }
    public List<StockAdjustmentLineItem> getLines() { return new ArrayList<>(lines); }
}
