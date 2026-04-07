package com.solusi.erp.common.approval.domain.repository;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import java.util.Optional;

/**
 * Domain Repository Interface for ApprovalRequest.
 */
public interface ApprovalRequestRepository {
    ApprovalRequest save(ApprovalRequest request);
    Optional<ApprovalRequest> findById(Long id);
    Optional<ApprovalRequest> findByReference(String referenceType, Long referenceId);
    Page<ApprovalRequest> findPendingApprovals(Pageable pageable);
    Page<ApprovalRequest> findPendingApprovals(String keyword, Pageable pageable);
    long countPendingApprovals();
    Page<ApprovalRequest> findPendingApprovalsForApprover(Long approverPartyId, Pageable pageable);
    Page<ApprovalRequest> findPendingApprovalsForApprover(Long approverPartyId, String keyword, Pageable pageable);
    long countPendingApprovalsForApprover(Long approverPartyId);
    Page<ApprovalRequest> findAll(Pageable pageable);
    Page<ApprovalRequest> findAll(String keyword, Pageable pageable);
}
