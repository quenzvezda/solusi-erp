package com.solusi.erp.accountspayable.debitmemo.domain.model;

import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;

public final class DebitMemoLine {

    private final Long id;
    private final Long purchaseReturnLineId;
    private final Long productId;
    private final BigDecimal quantity;
    private final Long uomId;
    private final BigDecimal dppAmountOriginal;
    private final BigDecimal taxAmountOriginal;
    private final BigDecimal dppAmountBase;
    private final BigDecimal taxAmountBase;

    public DebitMemoLine(Long id,
                         Long purchaseReturnLineId,
                         Long productId,
                         BigDecimal quantity,
                         Long uomId,
                         BigDecimal dppAmountOriginal,
                         BigDecimal taxAmountOriginal,
                         BigDecimal dppAmountBase,
                         BigDecimal taxAmountBase) {
        validate(purchaseReturnLineId, productId, quantity, uomId,
                dppAmountOriginal, taxAmountOriginal, dppAmountBase, taxAmountBase);
        this.id = id;
        this.purchaseReturnLineId = purchaseReturnLineId;
        this.productId = productId;
        this.quantity = quantity;
        this.uomId = uomId;
        this.dppAmountOriginal = dppAmountOriginal;
        this.taxAmountOriginal = taxAmountOriginal;
        this.dppAmountBase = dppAmountBase;
        this.taxAmountBase = taxAmountBase;
    }

    private static void validate(Long purchaseReturnLineId,
                                 Long productId,
                                 BigDecimal quantity,
                                 Long uomId,
                                 BigDecimal dppAmountOriginal,
                                 BigDecimal taxAmountOriginal,
                                 BigDecimal dppAmountBase,
                                 BigDecimal taxAmountBase) {
        if (purchaseReturnLineId == null || productId == null || uomId == null) {
            throw new DomainException("msg.error.debit-memo.line.reference-required");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.debit-memo.line.quantity-positive");
        }
        if (isNegative(dppAmountOriginal) || isNegative(taxAmountOriginal)
                || isNegative(dppAmountBase) || isNegative(taxAmountBase)) {
            throw new DomainException("msg.error.debit-memo.line.amount-non-negative");
        }
        if (isZero(dppAmountOriginal) && isPositive(taxAmountOriginal)) {
            throw new DomainException("msg.error.debit-memo.line.dpp-required-for-tax");
        }
        if (isZero(dppAmountBase) && isPositive(taxAmountBase)) {
            throw new DomainException("msg.error.debit-memo.line.dpp-required-for-tax");
        }
    }

    private static boolean isNegative(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) < 0;
    }

    private static boolean isZero(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) == 0;
    }

    private static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public Long getId() {
        return id;
    }

    public Long getPurchaseReturnLineId() {
        return purchaseReturnLineId;
    }

    public Long getProductId() {
        return productId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public Long getUomId() {
        return uomId;
    }

    public BigDecimal getDppAmountOriginal() {
        return dppAmountOriginal;
    }

    public BigDecimal getTaxAmountOriginal() {
        return taxAmountOriginal;
    }

    public BigDecimal getDppAmountBase() {
        return dppAmountBase;
    }

    public BigDecimal getTaxAmountBase() {
        return taxAmountBase;
    }
}

