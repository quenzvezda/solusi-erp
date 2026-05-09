package com.solusi.erp.inventory.goodsreceipt.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pur_goods_receipts")
@Getter @Setter @NoArgsConstructor
public class GoodsReceiptEntity extends BaseModel {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 40)
    private GoodsReceiptReferenceType referenceType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "facility_id")
    private Long facilityId;

    @Column(name = "currency_id", nullable = false)
    private Long currencyId;

    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsReceiptLineEntity> lines = new ArrayList<>();

    public Long getPoId() {
        return referenceType == GoodsReceiptReferenceType.PURCHASE_ORDER ? referenceId : null;
    }

    public void setPoId(Long poId) {
        this.referenceType = GoodsReceiptReferenceType.PURCHASE_ORDER;
        this.referenceId = poId;
    }
}
