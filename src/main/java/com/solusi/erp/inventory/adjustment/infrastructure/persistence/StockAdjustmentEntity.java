package com.solusi.erp.inventory.adjustment.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.core.model.CurrencyAmount;
import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inv_stock_adjustments")
@Getter
@Setter
@NoArgsConstructor
public class StockAdjustmentEntity extends BaseModel {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdjustmentStatus status = AdjustmentStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "facility_id")
    private Long facilityId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "currencyId", column = @Column(name = "currency_id")),
        @AttributeOverride(name = "exchangeRate", column = @Column(name = "total_exchange_rate")),
        @AttributeOverride(name = "originalAmount", column = @Column(name = "total_amount_original")),
        @AttributeOverride(name = "localAmount", column = @Column(name = "total_amount_local"))
    })
    private CurrencyAmount totalCost = new CurrencyAmount();

    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockAdjustmentLineEntity> lines = new ArrayList<>();
}
