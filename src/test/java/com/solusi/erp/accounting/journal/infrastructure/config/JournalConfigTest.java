package com.solusi.erp.accounting.journal.infrastructure.config;

import com.solusi.erp.accounting.coa.domain.port.CoaPostingValidator;
import com.solusi.erp.accounting.journal.application.usecase.command.*;
import com.solusi.erp.accounting.journal.application.usecase.query.FindJournalEntriesUseCase;
import com.solusi.erp.accounting.journal.application.usecase.query.GetJournalEntryDetailUseCase;
import com.solusi.erp.accounting.journal.domain.port.JournalEntryQueryPort;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalEntryJpaRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.master.currency.domain.port.CurrencyPostingValidator;
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
@ContextConfiguration(classes = {JournalConfig.class, JournalConfigTest.MocksConfig.class})
class JournalConfigTest {

    @Autowired private JournalEntryRepository journalEntryRepository;
    @Autowired private JournalEntryQueryPort journalEntryQueryPort;
    @Autowired private PostJournalForEventUseCase postJournalForEventUseCase;
    @Autowired private CreateManualJournalUseCase createManualJournalUseCase;
    @Autowired private UpdateManualJournalUseCase updateManualJournalUseCase;
    @Autowired private DeleteManualJournalUseCase deleteManualJournalUseCase;
    @Autowired private PostManualJournalUseCase postManualJournalUseCase;
    @Autowired private ReverseManualJournalUseCase reverseManualJournalUseCase;
    @Autowired private FindJournalEntriesUseCase findJournalEntriesUseCase;
    @Autowired private GetJournalEntryDetailUseCase getJournalEntryDetailUseCase;

    @Test
    void should_register_journal_beans() {
        assertThat(journalEntryRepository).isNotNull();
        assertThat(journalEntryQueryPort).isNotNull();
        assertThat(postJournalForEventUseCase).isNotNull();
        assertThat(createManualJournalUseCase).isNotNull();
        assertThat(updateManualJournalUseCase).isNotNull();
        assertThat(deleteManualJournalUseCase).isNotNull();
        assertThat(postManualJournalUseCase).isNotNull();
        assertThat(reverseManualJournalUseCase).isNotNull();
        assertThat(findJournalEntriesUseCase).isNotNull();
        assertThat(getJournalEntryDetailUseCase).isNotNull();
    }

    @Configuration
    static class MocksConfig {
        @Bean JournalEntryJpaRepository journalEntryJpaRepository() { return mock(JournalEntryJpaRepository.class); }
        @Bean SchemaRepository schemaRepository() { return mock(SchemaRepository.class); }
        @Bean CoaPostingValidator coaPostingValidator() { return mock(CoaPostingValidator.class); }
        @Bean CurrencyPostingValidator currencyPostingValidator() { return mock(CurrencyPostingValidator.class); }
        @Bean EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase() { return mock(EnsureOpenPeriodForDateUseCase.class); }
        @Bean PlatformTransactionManager platformTransactionManager() { return mock(PlatformTransactionManager.class); }
    }
}
