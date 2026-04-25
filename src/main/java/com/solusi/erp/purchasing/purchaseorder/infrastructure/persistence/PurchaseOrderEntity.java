package com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pur_purchase_orders")
@Getter @Setter @NoArgsConstructor
public class PurchaseOrderEntity extends BaseModel {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "expected_date")
    private LocalDate expectedDate;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "facility_id")
    private Long facilityId;

    @Column(name = "currency_id", nullable = false)
    private Long currencyId;

    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "tax_id")
    private Long taxId;

    @Column(name = "tax_code", length = 50)
    private String taxCode;

    @Column(name = "tax_name", length = 150)
    private String taxName;

    @Column(name = "tax_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_calculation_mode", nullable = false, length = 20)
    private TaxCalculationMode taxCalculationMode = TaxCalculationMode.EXCLUSIVE;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PurchaseOrderStatus status = PurchaseOrderStatus.DRAFT;

    @Column(name = "payment_term_days", nullable = false)
    private int paymentTermDays = 30;

    @Column(name = "pr_id")
    private Long prId;

    @Enumerated(EnumType.STRING)
    @Column(name = "po_type", nullable = false, length = 10)
    private PurchaseOrderType poType = PurchaseOrderType.DIRECT;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderLineEntity> lines = new ArrayList<>();
}
