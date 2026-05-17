package com.solusi.erp.accountspayable.vendorpayment.infrastructure.persistence;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentStatus;
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
@Table(name = "ap_vendor_payments")
@Getter @Setter @NoArgsConstructor
public class VendorPaymentEntity extends BaseModel {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "currency_id", nullable = false)
    private Long currencyId;

    @Column(name = "bank_account_id", nullable = false)
    private Long bankAccountId;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "payment_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal paymentAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VendorPaymentStatus status;

    @Column(length = 255)
    private String reference;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "vendorPayment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorPaymentLineEntity> lines = new ArrayList<>();
}
