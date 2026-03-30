package com.solusi.erp.inventory.grid.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inv_grids", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"facility_id", "code"})
})
@Getter
@Setter
@NoArgsConstructor
public class GridEntity extends BaseModel {

    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Column(length = 50, nullable = false)
    private String code;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
