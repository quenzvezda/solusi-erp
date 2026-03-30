package com.solusi.erp.inventory.facility.infrastructure.persistence;

import com.solusi.erp.core.model.Address;
import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inv_facilities")
@Getter
@Setter
@NoArgsConstructor
public class FacilityEntity extends BaseModel {

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
