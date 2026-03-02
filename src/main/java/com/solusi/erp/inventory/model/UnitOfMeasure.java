package com.solusi.erp.inventory.model;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Unit of Measure Entity.
 * Mandate: AGENTS.md Section 4 (Auditing via BaseModel)
 */
@Entity
@Table(name = "unit_of_measures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasure extends BaseModel {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UomType type;
}
