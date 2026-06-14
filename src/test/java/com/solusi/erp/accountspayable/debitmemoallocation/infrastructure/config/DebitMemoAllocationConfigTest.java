package com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.config;

import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.journal.application.usecase.command.ReversePostedJournalUseCase;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.CancelDebitMemoAllocationUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.ConfirmDebitMemoAllocationUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.CreateDebitMemoAllocationUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.ReverseDebitMemoAllocationUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.UpdateDebitMemoAllocationUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationSelectorUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.FindDebitMemoAllocationHistoryUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.FindDebitMemoAllocationsUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.GetDebitMemoAllocationDetailUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.service.DebitMemoAllocationProrationService;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationJpaRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationPersistenceMapper;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.VendorBillPaymentUpdatePort;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
    private CreateDebitMemoAllocationUseCase createDebitMemoAllocationUseCase;

    @Autowired
    private UpdateDebitMemoAllocationUseCase updateDebitMemoAllocationUseCase;

    @Autowired
    private CancelDebitMemoAllocationUseCase cancelDebitMemoAllocationUseCase;

    @Autowired
    private ConfirmDebitMemoAllocationUseCase confirmDebitMemoAllocationUseCase;

    @Autowired
    private ReverseDebitMemoAllocationUseCase reverseDebitMemoAllocationUseCase;

    @Autowired
    private FindDebitMemoAllocationsUseCase findDebitMemoAllocationsUseCase;

    @Autowired
    private GetDebitMemoAllocationDetailUseCase getDebitMemoAllocationDetailUseCase;

    @Autowired
    private FindDebitMemoAllocationHistoryUseCase findDebitMemoAllocationHistoryUseCase;

    @Autowired
    private DebitMemoAllocationSelectorUseCase debitMemoAllocationSelectorUseCase;

    @Test
    void should_register_debit_memo_allocation_beans() {
        assertThat(repository).isNotNull();
        assertThat(prorationService).isNotNull();
        assertThat(createDebitMemoAllocationUseCase).isNotNull();
        assertThat(updateDebitMemoAllocationUseCase).isNotNull();
        assertThat(cancelDebitMemoAllocationUseCase).isNotNull();
        assertThat(confirmDebitMemoAllocationUseCase).isNotNull();
        assertThat(reverseDebitMemoAllocationUseCase).isNotNull();
        assertThat(findDebitMemoAllocationsUseCase).isNotNull();
        assertThat(getDebitMemoAllocationDetailUseCase).isNotNull();
        assertThat(findDebitMemoAllocationHistoryUseCase).isNotNull();
        assertThat(debitMemoAllocationSelectorUseCase).isNotNull();
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

        @Bean
        NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
            return mock(NamedParameterJdbcTemplate.class);
        }

        @Bean
        SequenceGeneratorService sequenceGeneratorService() {
            return mock(SequenceGeneratorService.class);
        }

        @Bean
        DebitMemoRepository debitMemoRepository() {
            return mock(DebitMemoRepository.class);
        }

        @Bean
        PostJournalForEventUseCase postJournalForEventUseCase() {
            return mock(PostJournalForEventUseCase.class);
        }

        @Bean
        JournalEntryRepository journalEntryRepository() {
            return mock(JournalEntryRepository.class);
        }

        @Bean
        ReversePostedJournalUseCase reversePostedJournalUseCase() {
            return mock(ReversePostedJournalUseCase.class);
        }

        @Bean
        VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort() {
            return mock(VendorBillPaymentUpdatePort.class);
        }

        @Bean
        EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase() {
            return mock(EnsureOpenPeriodForDateUseCase.class);
        }
    }
}
