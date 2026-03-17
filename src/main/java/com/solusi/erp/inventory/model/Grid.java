package com.solusi.erp.inventory.model;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Blocking/Area management within a facility (e.g., Aisle A, Cold Zone).
 */
@Entity
@Table(name = "inv_grids", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"facility_id", "code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Grid extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(length = 50, nullable = false)
    private String code;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
