package com.solusi.erp.common.approval.infrastructure.persistence;

import com.solusi.erp.common.approval.domain.model.ApprovalStatus;
import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for ApprovalRequest.
 */
@Entity
@Table(name = "appr_requests")
@Getter
@Setter
public class ApprovalRequestEntity extends BaseModel {

    @Column(name = "reference_type", nullable = false, length = 50)
    private String referenceType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus status;

    @Column(name = "current_approver_id")
    private Long currentApproverId;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApprovalHistoryEntity> histories = new ArrayList<>();
}
