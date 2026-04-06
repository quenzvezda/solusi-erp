package com.solusi.erp.common.approval.application.usecase;

import com.solusi.erp.common.approval.application.port.ApprovalEventPublisher;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.core.exception.DomainException;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of ProcessApprovalUseCase.
 */
@RequiredArgsConstructor
public class ProcessApprovalUseCaseImpl implements ProcessApprovalUseCase {

    private final ApprovalRequestRepository repository;
    private final ApprovalEventPublisher eventPublisher;

    @Override
    public ApprovalRequest approve(Long requestId, Long actorId, String notes) {
        ApprovalRequest request = repository.findById(requestId)
            .orElseThrow(() -> new DomainException("msg.error.approval.not-found"));
            
        request.approve(actorId, notes);
        ApprovalRequest saved = repository.save(request);
        
        // Notify the world that this document is officially approved!
        eventPublisher.publishCompleted(saved.getReferenceType(), saved.getReferenceId());
        
        return saved;
    }

    @Override
    public ApprovalRequest reject(Long requestId, Long actorId, String notes) {
        ApprovalRequest request = repository.findById(requestId)
            .orElseThrow(() -> new DomainException("msg.error.approval.not-found"));
            
        request.reject(actorId, notes);
        ApprovalRequest saved = repository.save(request);
        
        // Notify that it's rejected
        eventPublisher.publishRejected(saved.getReferenceType(), saved.getReferenceId());
        
        return saved;
    }

    @Override
    public ApprovalRequest forward(Long requestId, Long actorId, Long targetApproverId, String notes) {
        ApprovalRequest request = repository.findById(requestId)
            .orElseThrow(() -> new DomainException("msg.error.approval.not-found"));

        request.forward(actorId, targetApproverId, notes);
        return repository.save(request);
    }

    @Override
    public ApprovalRequest approveAndForward(Long requestId, Long actorId, Long targetApproverId, String notes) {
        ApprovalRequest request = repository.findById(requestId)
            .orElseThrow(() -> new DomainException("msg.error.approval.not-found"));

        request.approveAndForward(actorId, targetApproverId, notes);
        return repository.save(request);
    }
}
