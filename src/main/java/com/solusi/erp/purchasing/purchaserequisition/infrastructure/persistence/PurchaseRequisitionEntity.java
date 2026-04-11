package com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pur_purchase_requisitions")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseRequisitionEntity extends BaseModel {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "facility_id")
    private Long facilityId;

    @Column(length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseRequisitionPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseRequisitionStatus status = PurchaseRequisitionStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseRequisitionLineEntity> lines = new ArrayList<>();
}
