package com.solusi.erp.common.approval.infrastructure.config;

import com.solusi.erp.common.approval.application.port.ApprovalEventPublisher;
import com.solusi.erp.common.approval.application.usecase.*;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.common.approval.signature.application.usecase.GetApprovalSignatureUrlUseCase;
import com.solusi.erp.common.approval.signature.application.usecase.GetApprovalSignatureUrlUseCaseImpl;
import com.solusi.erp.common.approval.signature.application.usecase.SaveApprovalSignatureUseCase;
import com.solusi.erp.common.approval.signature.application.usecase.SaveApprovalSignatureUseCaseImpl;
import com.solusi.erp.common.approval.signature.domain.repository.ApprovalSignatureRepository;
import com.solusi.erp.core.storage.domain.port.StorageProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composition Root for the Approval Module.
 */
@Configuration
public class ApprovalConfig {

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Bean
    public CreateApprovalRequestUseCase createApprovalRequestUseCase(
            ApprovalRequestRepository repository,
            TransactionTemplate transactionTemplate) {
        CreateApprovalRequestUseCase pureUseCase = new CreateApprovalRequestUseCaseImpl(repository);
        return (refType, refId, requester, approverId) ->
            transactionTemplate.execute(status -> pureUseCase.execute(refType, refId, requester, approverId));
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

            @Override
            public com.solusi.erp.common.approval.domain.model.ApprovalRequest forward(Long id, Long actorId, Long targetApproverId, String notes) {
                return transactionTemplate.execute(status -> pureUseCase.forward(id, actorId, targetApproverId, notes));
            }

            @Override
            public com.solusi.erp.common.approval.domain.model.ApprovalRequest approveAndForward(Long id, Long actorId, Long targetApproverId, String notes) {
                return transactionTemplate.execute(status -> pureUseCase.approveAndForward(id, actorId, targetApproverId, notes));
            }
        };
    }

    @Bean
    public SaveApprovalSignatureUseCase saveApprovalSignatureUseCase(
            ApprovalSignatureRepository signatureRepository,
            ApprovalRequestRepository approvalRequestRepository,
            StorageProvider storageProvider,
            TransactionTemplate transactionTemplate) {
        SaveApprovalSignatureUseCase pureUseCase = new SaveApprovalSignatureUseCaseImpl(
                signatureRepository, approvalRequestRepository, storageProvider, bucketName);
        return (requestId, signatureBase64, signerUserId) ->
                transactionTemplate.execute(status -> pureUseCase.execute(requestId, signatureBase64, signerUserId));
    }

    @Bean
    public GetApprovalSignatureUrlUseCase getApprovalSignatureUrlUseCase(
            ApprovalSignatureRepository signatureRepository) {
        return new GetApprovalSignatureUrlUseCaseImpl(signatureRepository);
    }
}
