package com.solusi.erp.purchasing.purchaseorder.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PurchaseOrder {

    private final AuditMetadata metadata;
    private final String code;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private final Long supplierId;
    private Long facilityId;
    private Long currencyId;
    private BigDecimal exchangeRate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private PurchaseOrderStatus status;
    private int paymentTermDays;
    private final Long prId;
    private final PurchaseOrderType poType;
    private Long taxId;
    private String taxCode;
    private String taxName;
    private BigDecimal taxRate;
    private TaxCalculationMode taxCalculationMode;
    private String note;
    private boolean active;
    private List<PurchaseOrderLine> lines;

    public PurchaseOrder(AuditMetadata metadata, String code,
                          LocalDate orderDate, LocalDate expectedDate,
                          Long supplierId, Long facilityId, Long currencyId,
                          BigDecimal exchangeRate,
                          BigDecimal subtotal, BigDecimal taxAmount, BigDecimal totalAmount,
                          PurchaseOrderStatus status,
                          int paymentTermDays, Long prId, PurchaseOrderType poType,
                          String note, boolean active,
                          List<PurchaseOrderLine> lines) {
        this(
                metadata, code, orderDate, expectedDate, supplierId, facilityId, currencyId,
                exchangeRate, subtotal, taxAmount, totalAmount, status, paymentTermDays, prId, poType,
                null, null, null, BigDecimal.ZERO, TaxCalculationMode.EXCLUSIVE,
                note, active, lines
        );
    }

    public PurchaseOrder(AuditMetadata metadata, String code,
                          LocalDate orderDate, LocalDate expectedDate,
                          Long supplierId, Long facilityId, Long currencyId,
                          BigDecimal exchangeRate,
                          BigDecimal subtotal, BigDecimal taxAmount, BigDecimal totalAmount,
                          PurchaseOrderStatus status,
                          int paymentTermDays, Long prId, PurchaseOrderType poType,
                          Long taxId, String taxCode, String taxName, BigDecimal taxRate,
                          TaxCalculationMode taxCalculationMode,
                          String note, boolean active,
                          List<PurchaseOrderLine> lines) {
        this.metadata = metadata;
        this.code = code;
        this.orderDate = orderDate;
        this.expectedDate = expectedDate;
        this.supplierId = supplierId;
        this.facilityId = facilityId;
        this.currencyId = currencyId;
        this.exchangeRate = exchangeRate;
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentTermDays = paymentTermDays;
        this.prId = prId;
        this.poType = poType != null ? poType : PurchaseOrderType.DIRECT;
        this.taxId = taxId;
        this.taxCode = taxCode;
        this.taxName = taxName;
        this.taxRate = taxRate != null ? taxRate : BigDecimal.ZERO;
        this.taxCalculationMode = taxCalculationMode != null ? taxCalculationMode : TaxCalculationMode.EXCLUSIVE;
        this.note = note;
        this.active = active;
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    public static PurchaseOrder createNew(String code, LocalDate orderDate,
                                           LocalDate expectedDate,
                                            Long supplierId, Long facilityId,
                                            Long currencyId, BigDecimal exchangeRate,
                                            int paymentTermDays, Long prId,
                                            PurchaseOrderType poType,
                                            String note,
                                            List<PurchaseOrderLine> lines) {
        return createNew(code, orderDate, expectedDate, supplierId, facilityId, currencyId, exchangeRate,
                paymentTermDays, prId, poType, null, null, null, BigDecimal.ZERO,
                TaxCalculationMode.EXCLUSIVE, note, lines);
    }

    public static PurchaseOrder createNew(String code, LocalDate orderDate,
                                           LocalDate expectedDate,
                                           Long supplierId, Long facilityId,
                                           Long currencyId, BigDecimal exchangeRate,
                                           int paymentTermDays, Long prId,
                                           PurchaseOrderType poType,
                                           Long taxId, String taxCode, String taxName, BigDecimal taxRate,
                                           TaxCalculationMode taxCalculationMode,
                                           String note,
                                           List<PurchaseOrderLine> lines) {
        validateExchangeRate(exchangeRate);
        validateExpectedDate(orderDate, expectedDate);
        validatePoType(poType, prId);

        PurchaseOrder po = new PurchaseOrder(
            AuditMetadata.empty(), code, orderDate, expectedDate,
            supplierId, facilityId, currencyId, exchangeRate,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            PurchaseOrderStatus.DRAFT,
            paymentTermDays, prId, poType,
            taxId, taxCode, taxName, taxRate, taxCalculationMode,
            note, true,
            lines
        );
        po.recalculateTotals();
        return po;
    }

    public void update(LocalDate orderDate, LocalDate expectedDate,
                       Long facilityId, Long currencyId,
                       BigDecimal exchangeRate, int paymentTermDays,
                       String note, List<PurchaseOrderLine> lines) {
        update(orderDate, expectedDate, facilityId, currencyId, exchangeRate, paymentTermDays,
                this.taxId, this.taxCode, this.taxName, this.taxRate, this.taxCalculationMode, note, lines);
    }

    public void update(LocalDate orderDate, LocalDate expectedDate,
                       Long facilityId, Long currencyId,
                       BigDecimal exchangeRate, int paymentTermDays,
                       Long taxId, String taxCode, String taxName, BigDecimal taxRate,
                       TaxCalculationMode taxCalculationMode,
                       String note, List<PurchaseOrderLine> lines) {
        if (!status.canUpdate()) {
            throw new DomainException("msg.error.po.update.not.draft");
        }
        this.orderDate = orderDate;
        this.expectedDate = expectedDate;
        this.facilityId = facilityId;
        this.currencyId = currencyId;
        this.exchangeRate = exchangeRate;
        this.paymentTermDays = paymentTermDays;
        this.taxId = taxId;
        this.taxCode = taxCode;
        this.taxName = taxName;
        this.taxRate = taxRate != null ? taxRate : BigDecimal.ZERO;
        this.taxCalculationMode = taxCalculationMode != null ? taxCalculationMode : TaxCalculationMode.EXCLUSIVE;
        this.note = note;
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
        recalculateTotals();
    }

    public void submit() {
        if (!status.canSubmit()) {
            throw new DomainException("msg.error.po.submit.invalid.status");
        }
        if (lines == null || lines.isEmpty()) {
            throw new DomainException("msg.error.po.submit.no.lines");
        }
        this.status = PurchaseOrderStatus.SUBMITTED;
    }

    public void approve() {
        this.status = PurchaseOrderStatus.APPROVED;
    }

    public void reject() {
        this.status = PurchaseOrderStatus.REJECTED;
    }

    public void send() {
        if (!status.canSend()) {
            throw new DomainException("msg.error.po.send.invalid.status");
        }
        this.status = PurchaseOrderStatus.SENT;
    }

    public void recordReceipt(Map<Long, BigDecimal> receivedByLineId) {
        if (!status.canReceive()) {
            throw new DomainException("msg.error.gr.po.invalid.status");
        }
        if (receivedByLineId == null || receivedByLineId.isEmpty()) {
            throw new DomainException("msg.error.gr.po.receipt.lines.required");
        }
        boolean matchedLine = false;
        for (PurchaseOrderLine line : lines) {
            Long lineId = line.getId();
            if (lineId != null && receivedByLineId.containsKey(lineId)) {
                matchedLine = true;
                line.validateReceipt(receivedByLineId.get(lineId));
            }
        }
        if (!matchedLine) {
            throw new DomainException("msg.error.gr.po.receipt.lines.required");
        }
        for (PurchaseOrderLine line : lines) {
            Long lineId = line.getId();
            if (lineId != null && receivedByLineId.containsKey(lineId)) {
                line.receive(receivedByLineId.get(lineId));
            }
        }
        boolean fullyReceived = lines.stream()
                .allMatch(line -> line.getOutstandingQuantity().compareTo(BigDecimal.ZERO) == 0);
        this.status = fullyReceived ? PurchaseOrderStatus.FULLY_RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED;
    }

    public void cancel() {
        if (!status.canCancel()) {
            throw new DomainException("msg.error.po.cancel.invalid.status");
        }
        this.status = PurchaseOrderStatus.CANCELLED;
    }

    public void deactivate() {
        if (!status.canDelete()) {
            throw new DomainException("msg.error.po.delete.not.draft");
        }
        this.active = false;
    }

    private void recalculateTotals() {
        if (hasHeaderTaxSnapshot()) {
            BigDecimal normalizedRate = this.taxRate.movePointLeft(2);
            TaxCalculationMode mode = this.taxCalculationMode != null
                    ? this.taxCalculationMode
                    : TaxCalculationMode.EXCLUSIVE;
            this.lines = this.lines.stream()
                    .map(line -> line.recalculate(normalizedRate, mode))
                    .toList();
        }
        this.subtotal = lines.stream()
            .map(PurchaseOrderLine::getLineSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.taxAmount = lines.stream()
            .map(PurchaseOrderLine::getLineTax)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = this.subtotal.add(this.taxAmount);
    }

    public static PurchaseOrder rehydrate(AuditMetadata metadata, String code,
                                          LocalDate orderDate, LocalDate expectedDate,
                                          Long supplierId, Long facilityId, Long currencyId, BigDecimal exchangeRate,
                                          BigDecimal subtotal, BigDecimal taxAmount, BigDecimal totalAmount,
                                          PurchaseOrderStatus status, int paymentTermDays, Long prId, PurchaseOrderType poType,
                                          Long taxId, String taxCode, String taxName, BigDecimal taxRate,
                                          TaxCalculationMode taxCalculationMode,
                                          String note, boolean active, List<PurchaseOrderLine> lines) {
        return new PurchaseOrder(
                metadata, code, orderDate, expectedDate, supplierId, facilityId, currencyId, exchangeRate,
                subtotal, taxAmount, totalAmount, status, paymentTermDays, prId, poType,
                taxId, taxCode, taxName, taxRate, taxCalculationMode, note, active, lines
        );
    }

    private boolean hasHeaderTaxSnapshot() {
        return taxId != null || taxCode != null || taxName != null
                || (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) > 0);
    }

    private static void validateExchangeRate(BigDecimal exchangeRate) {
        if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.po.exchangerate.positive");
        }
    }

    private static void validateExpectedDate(LocalDate orderDate, LocalDate expectedDate) {
        if (expectedDate != null && expectedDate.isBefore(orderDate)) {
            throw new DomainException("msg.error.po.expecteddate.before.orderdate");
        }
    }

    private static void validatePoType(PurchaseOrderType poType, Long prId) {
        if (PurchaseOrderType.STANDARD == poType && prId == null) {
            throw new DomainException("msg.error.po.standard.pr.required");
        }
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public LocalDate getOrderDate() { return orderDate; }
    public LocalDate getExpectedDate() { return expectedDate; }
    public Long getSupplierId() { return supplierId; }
    public Long getFacilityId() { return facilityId; }
    public Long getCurrencyId() { return currencyId; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public PurchaseOrderStatus getStatus() { return status; }
    public int getPaymentTermDays() { return paymentTermDays; }
    public Long getPrId() { return prId; }
    public PurchaseOrderType getPoType() { return poType; }
    public Long getTaxId() { return taxId; }
    public String getTaxCode() { return taxCode; }
    public String getTaxName() { return taxName; }
    public BigDecimal getTaxRate() { return taxRate; }
    public TaxCalculationMode getTaxCalculationMode() { return taxCalculationMode; }
    public String getNote() { return note; }
    public boolean isActive() { return active; }
    public List<PurchaseOrderLine> getLines() { return Collections.unmodifiableList(lines); }
}
