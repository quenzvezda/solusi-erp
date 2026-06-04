package com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillDocumentStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillSettlementStatus;
import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ap_vendor_bills")
@Getter @Setter @NoArgsConstructor
public class VendorBillEntity extends BaseModel {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "vendor_invoice_number", nullable = false, length = 100)
    private String vendorInvoiceNumber;

    @Column(name = "bill_date", nullable = false)
    private LocalDate billDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "currency_id", nullable = false)
    private Long currencyId;

    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_status", nullable = false, length = 30)
    private VendorBillDocumentStatus documentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", length = 30)
    private VendorBillSettlementStatus settlementStatus;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "notes", length = 500)
    private String notes;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorBillLineEntity> lines = new ArrayList<>();

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorBillGrRefEntity> grRefs = new ArrayList<>();
}
