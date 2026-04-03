package com.solusi.erp.inventory.container.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.inventory.shared.embeddable.Dimensions;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "inv_containers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"grid_id", "code"})
})
@Getter
@Setter
@NoArgsConstructor
public class ContainerEntity extends BaseModel {

    @Column(name = "grid_id", nullable = false)
    private Long gridId;

    @Column(length = 50, nullable = false)
    private String code;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(length = 100, unique = true)
    private String barcode;

    @Embedded
    private Dimensions dimensions;

    @Column(name = "max_weight", precision = 10, scale = 2)
    private BigDecimal maxWeight;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
