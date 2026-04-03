package com.solusi.erp.master.tax.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_subtract", nullable = false)
    private Boolean isSubtract;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public Tax() {
        this.isSubtract = false;
        this.isActive = true;
    }
}
