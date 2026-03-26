package com.solusi.erp.common.approval.domain.repository;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import java.util.Optional;

/**
 * Domain Repository Interface for ApprovalRequest.
 */
public interface ApprovalRequestRepository {
    ApprovalRequest save(ApprovalRequest request);
    Optional<ApprovalRequest> findById(Long id);
    Optional<ApprovalRequest> findByReference(String referenceType, Long referenceId);
}
