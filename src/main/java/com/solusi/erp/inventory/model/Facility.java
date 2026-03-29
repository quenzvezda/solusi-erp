package com.solusi.erp.inventory.model;

import com.solusi.erp.core.model.Address;
import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Top-level building/warehouse management.
 * Mandate: AGENTS.md Section 4 & 5
 */
@Entity
@Table(name = "inv_facilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Facility extends BaseModel {

    @Column(length = 50, nullable = false, unique = true)
    private String code;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Embedded
    private Address address;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
