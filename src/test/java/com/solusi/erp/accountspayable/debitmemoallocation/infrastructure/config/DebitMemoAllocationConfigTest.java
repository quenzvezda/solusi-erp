package com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.config;

import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.FindDebitMemoAllocationHistoryUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.FindDebitMemoAllocationsUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.GetDebitMemoAllocationDetailUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.service.DebitMemoAllocationProrationService;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationJpaRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DebitMemoAllocationConfig.class, DebitMemoAllocationConfigTest.MocksConfig.class})
class DebitMemoAllocationConfigTest {

    @Autowired
    private DebitMemoAllocationRepository repository;

    @Autowired
    private DebitMemoAllocationProrationService prorationService;

    @Autowired
    private FindDebitMemoAllocationsUseCase findDebitMemoAllocationsUseCase;

    @Autowired
    private GetDebitMemoAllocationDetailUseCase getDebitMemoAllocationDetailUseCase;

    @Autowired
    private FindDebitMemoAllocationHistoryUseCase findDebitMemoAllocationHistoryUseCase;

    @Test
    void should_register_debit_memo_allocation_beans() {
        assertThat(repository).isNotNull();
        assertThat(prorationService).isNotNull();
        assertThat(findDebitMemoAllocationsUseCase).isNotNull();
        assertThat(getDebitMemoAllocationDetailUseCase).isNotNull();
        assertThat(findDebitMemoAllocationHistoryUseCase).isNotNull();
    }

    @Configuration
    static class MocksConfig {
        @Bean
        DebitMemoAllocationJpaRepository debitMemoAllocationJpaRepository() {
            return mock(DebitMemoAllocationJpaRepository.class);
        }

        @Bean
        DebitMemoAllocationPersistenceMapper debitMemoAllocationPersistenceMapper() {
            return mock(DebitMemoAllocationPersistenceMapper.class);
        }

        @Bean
        PlatformTransactionManager platformTransactionManager() {
            return mock(PlatformTransactionManager.class);
        }
    }
}
