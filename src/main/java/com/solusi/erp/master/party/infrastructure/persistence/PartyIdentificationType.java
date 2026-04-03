package com.solusi.erp.master.party.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lookup table for Party Identification Types (e.g. KTP, NPWP).
 */
@Entity
@Table(name = "party_id_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartyIdentificationType extends BaseModel {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;
}
