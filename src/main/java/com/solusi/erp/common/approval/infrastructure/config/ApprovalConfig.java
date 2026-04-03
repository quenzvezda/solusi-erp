package com.solusi.erp.common.approval.infrastructure.config;

import com.solusi.erp.common.approval.application.port.ApprovalEventPublisher;
import com.solusi.erp.common.approval.application.usecase.*;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composition Root for the Approval Module.
 */
@Configuration
public class ApprovalConfig {

    @Bean
    public CreateApprovalRequestUseCase createApprovalRequestUseCase(
            ApprovalRequestRepository repository,
            TransactionTemplate transactionTemplate) {
        CreateApprovalRequestUseCase pureUseCase = new CreateApprovalRequestUseCaseImpl(repository);
        return (refType, refId, requester) -> 
            transactionTemplate.execute(status -> pureUseCase.execute(refType, refId, requester));
    }

    @Bean
    public ProcessApprovalUseCase processApprovalUseCase(
            ApprovalRequestRepository repository,
            ApprovalEventPublisher eventPublisher,
            TransactionTemplate transactionTemplate) {
        ProcessApprovalUseCase pureUseCase = new ProcessApprovalUseCaseImpl(repository, eventPublisher);
        return new ProcessApprovalUseCase() {
            @Override
            public com.solusi.erp.common.approval.domain.model.ApprovalRequest approve(Long id, Long actorId, String notes) {
                return transactionTemplate.execute(status -> pureUseCase.approve(id, actorId, notes));
            }

            @Override
            public com.solusi.erp.common.approval.domain.model.ApprovalRequest reject(Long id, Long actorId, String notes) {
                return transactionTemplate.execute(status -> pureUseCase.reject(id, actorId, notes));
            }
        };
    }
}
