package com.solusi.erp.master.model;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ContactMechanism linked to a Party.
 * Represents a named contact (person/channel) for a Business Partner.
 */
@Entity
@Table(name = "party_contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartyContact extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    /**
     * Descriptive label, e.g. "PIC Sales", "Direktur", "Gudang".
     */
    @Column(nullable = false, length = 100)
    private String label;

    @Column(length = 50)
    private String mobile;

    @Column(length = 50)
    private String phone;

    @Column(length = 100)
    private String email;

    /**
     * Soft-delete flag. True = active contact.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Marks the primary contact for this party.
     * Only one contact per party should have isDefault = true.
     */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
}
