package com.solusi.erp.master.tax.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "taxes")
@Getter
@Setter
public class Tax extends BaseModel {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal rate;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_mode", nullable = false, length = 20)
    private TaxCalculationMode calculationMode;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_subtract", nullable = false)
    private Boolean isSubtract;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public Tax() {
        this.calculationMode = TaxCalculationMode.EXCLUSIVE;
        this.isSubtract = false;
        this.isActive = true;
    }
}
