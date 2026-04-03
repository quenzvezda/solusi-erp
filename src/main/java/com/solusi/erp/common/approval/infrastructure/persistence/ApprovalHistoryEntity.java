package com.solusi.erp.common.approval.infrastructure.persistence;

import com.solusi.erp.common.approval.domain.model.ApprovalAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * JPA Entity for ApprovalHistory.
 */
@Entity
@Table(name = "appr_histories")
@Getter
@Setter
public class ApprovalHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ApprovalRequestEntity request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApprovalAction action;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "target_approver_id")
    private Long targetApproverId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "action_date", nullable = false)
    private LocalDateTime actionDate;
}
