package com.solusi.erp.inventory.uom.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.inventory.uom.domain.model.UomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "unit_of_measures")
@Getter
@Setter
@NoArgsConstructor
public class UomEntity extends BaseModel {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UomType type;
}
