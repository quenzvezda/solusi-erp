package com.solusi.erp.accountspayable.debitmemo.infrastructure.config;

import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.FindDebitMemoByPurchaseReturnUseCase;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.FindDebitMemosUseCase;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.GetDebitMemoDetailUseCase;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence.DebitMemoJpaRepository;
import com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence.DebitMemoPersistenceMapper;
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
@ContextConfiguration(classes = {DebitMemoConfig.class, DebitMemoConfigTest.MocksConfig.class})
class DebitMemoConfigTest {

    @Autowired
    private DebitMemoRepository debitMemoRepository;

    @Autowired
    private FindDebitMemosUseCase findDebitMemosUseCase;

    @Autowired
    private GetDebitMemoDetailUseCase getDebitMemoDetailUseCase;

    @Autowired
    private FindDebitMemoByPurchaseReturnUseCase findDebitMemoByPurchaseReturnUseCase;

    @Test
    void should_register_debit_memo_beans() {
        assertThat(debitMemoRepository).isNotNull();
        assertThat(findDebitMemosUseCase).isNotNull();
        assertThat(getDebitMemoDetailUseCase).isNotNull();
        assertThat(findDebitMemoByPurchaseReturnUseCase).isNotNull();
    }

    @Configuration
    static class MocksConfig {
        @Bean
        DebitMemoJpaRepository debitMemoJpaRepository() {
            return mock(DebitMemoJpaRepository.class);
        }

        @Bean
        DebitMemoPersistenceMapper debitMemoPersistenceMapper() {
            return mock(DebitMemoPersistenceMapper.class);
        }

        @Bean
        PlatformTransactionManager platformTransactionManager() {
            return mock(PlatformTransactionManager.class);
        }
    }
}

