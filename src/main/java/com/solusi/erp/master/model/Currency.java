package com.solusi.erp.master.model;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "master_currencies")
@Getter
@Setter
public class Currency extends BaseModel {

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false, unique = true, length = 10)
    private String alias;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    public Currency() {
        this.isActive = true;
        this.isDefault = false;
    }
}
