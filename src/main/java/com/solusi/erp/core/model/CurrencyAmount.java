package com.solusi.erp.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Reusable embeddable for currency reference and amount fields.
 * Keeps monetary values consistent without coupling to a currency entity.
 */
@Getter
@Setter
@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyAmount {

    @Column(name = "currency_id")
    private Long currencyId;

    @Column(name = "exchange_rate", precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "original_amount", precision = 19, scale = 4)
    private BigDecimal originalAmount;

    @Column(name = "local_amount", precision = 19, scale = 4)
    private BigDecimal localAmount;
}
