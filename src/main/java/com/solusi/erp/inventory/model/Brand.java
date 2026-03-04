package com.solusi.erp.inventory.model;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Brand Entity.
 * Mandate: AGENTS.md Section 4 (Auditing via BaseModel)
 */
@Entity
@Table(name = "brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Brand extends BaseModel {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String note;
}
