package com.solusi.erp.common.approval.infrastructure.config;

import com.solusi.erp.common.approval.application.port.ApprovalEventPublisher;
import com.solusi.erp.common.approval.application.service.ApprovalActionOccurredEventFactory;
import com.solusi.erp.common.approval.application.service.ApprovalNotificationTargetResolver;
import com.solusi.erp.common.approval.application.usecase.CancelApprovalRequestUseCase;
import com.solusi.erp.common.approval.application.usecase.CreateApprovalRequestUseCase;
import com.solusi.erp.common.approval.application.usecase.ProcessApprovalUseCase;
import com.solusi.erp.common.approval.application.usecase.query.FindApprovalRequestByReferenceUseCase;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.common.approval.signature.application.usecase.GetApprovalSignatureUrlUseCase;
import com.solusi.erp.common.approval.signature.application.usecase.SaveApprovalSignatureUseCase;
import com.solusi.erp.common.approval.signature.domain.repository.ApprovalSignatureRepository;
import com.solusi.erp.core.storage.domain.port.StorageProvider;
import com.solusi.erp.core.storage.infrastructure.config.MinioProperties;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.security.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ApprovalConfig.class, ApprovalConfigTest.MocksConfig.class})
class ApprovalConfigTest {

    @Autowired
    private CreateApprovalRequestUseCase createApprovalRequestUseCase;

    @Autowired
    private ProcessApprovalUseCase processApprovalUseCase;

    @Autowired
    private CancelApprovalRequestUseCase cancelApprovalRequestUseCase;

    @Autowired
    private SaveApprovalSignatureUseCase saveApprovalSignatureUseCase;

    @Autowired
    private GetApprovalSignatureUrlUseCase getApprovalSignatureUrlUseCase;

    @Autowired
    private FindApprovalRequestByReferenceUseCase findApprovalRequestByReferenceUseCase;

    @Autowired
    private ApprovalNotificationTargetResolver approvalNotificationTargetResolver;

    @Autowired
    private ApprovalActionOccurredEventFactory approvalActionOccurredEventFactory;

    @Test
    void wiresApprovalUseCasesAndEventServices() {
        assertThat(createApprovalRequestUseCase).isNotNull();
        assertThat(processApprovalUseCase).isNotNull();
        assertThat(cancelApprovalRequestUseCase).isNotNull();
        assertThat(saveApprovalSignatureUseCase).isNotNull();
        assertThat(getApprovalSignatureUrlUseCase).isNotNull();
        assertThat(findApprovalRequestByReferenceUseCase).isNotNull();
        assertThat(approvalNotificationTargetResolver).isNotNull();
        assertThat(approvalActionOccurredEventFactory).isNotNull();
    }

    @Configuration
    static class MocksConfig {

        @Bean
        ApprovalRequestRepository approvalRequestRepository() {
            return mock(ApprovalRequestRepository.class);
        }

        @Bean
        ApprovalEventPublisher approvalEventPublisher() {
            return mock(ApprovalEventPublisher.class);
        }

        @Bean
        TransactionTemplate transactionTemplate() {
            return new TransactionTemplate(mock(PlatformTransactionManager.class));
        }

        @Bean
        ApprovalSignatureRepository approvalSignatureRepository() {
            return mock(ApprovalSignatureRepository.class);
        }

        @Bean
        StorageProvider storageProvider() {
            return mock(StorageProvider.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        PartyLookupProvider partyLookupProvider() {
            return mock(PartyLookupProvider.class);
        }

        @Bean
        Clock messagingClock() {
            return Clock.systemUTC();
        }

        @Bean
        MinioProperties minioProperties() {
            MinioProperties properties = new MinioProperties();
            properties.setBuckets(Map.of("signatures", "approval-signatures"));
            return properties;
        }
    }
}
