package com.solusi.erp.master.geographic.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.master.shared.model.GeographicType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Geographic entity for Countries, States/Provinces, and Cities.
 * Mandate: AGENTS.md Section 4
 */
@Entity
@Table(name = "geographics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Geographic extends BaseModel {

    @Column(length = 50, nullable = false, unique = true)
    private String code;

    @Column(length = 150, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GeographicType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Geographic parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Geographic> children = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
