package com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ap_debit_memo_lines")
@Getter
@Setter
@NoArgsConstructor
public class DebitMemoLineEntity extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debit_memo_id", nullable = false)
    private DebitMemoEntity debitMemo;

    @Column(name = "purchase_return_line_id", nullable = false)
    private Long purchaseReturnLineId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "uom_id", nullable = false)
    private Long uomId;

    @Column(name = "dpp_amount_original", nullable = false, precision = 19, scale = 4)
    private BigDecimal dppAmountOriginal = BigDecimal.ZERO;

    @Column(name = "tax_amount_original", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmountOriginal = BigDecimal.ZERO;

    @Column(name = "dpp_amount_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal dppAmountBase = BigDecimal.ZERO;

    @Column(name = "tax_amount_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmountBase = BigDecimal.ZERO;
}

