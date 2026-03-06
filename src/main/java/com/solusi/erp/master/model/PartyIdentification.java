package com.solusi.erp.master.model;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Identification documents linked to a Party.
 */
@Entity
@Table(name = "party_identifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartyIdentification extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_id", nullable = false)
    private PartyIdentificationType type;

    @Column(name = "id_number", nullable = false, length = 100)
    private String idNumber;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;
}
