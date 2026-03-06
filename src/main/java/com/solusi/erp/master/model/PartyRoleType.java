package com.solusi.erp.master.model;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lookup table for Party Roles (e.g. CUSTOMER, SUPPLIER).
 */
@Entity
@Table(name = "party_role_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartyRoleType extends BaseModel {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;
}
