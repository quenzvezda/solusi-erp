package com.solusi.erp.accounting.journal.infrastructure.config;

import com.solusi.erp.accounting.coa.domain.port.CoaPostingValidator;
import com.solusi.erp.accounting.journal.application.usecase.command.*;
import com.solusi.erp.accounting.journal.application.usecase.query.*;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalEntryFilter;
import com.solusi.erp.accounting.journal.domain.port.JournalEntryQueryPort;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.journal.infrastructure.adapter.JournalEntryQueryPortImpl;
import com.solusi.erp.accounting.journal.infrastructure.adapter.JournalEntryRepositoryImpl;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalEntryJpaRepository;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalPersistenceMapper;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.currency.domain.port.CurrencyPostingValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.Optional;

@Configuration
public class JournalConfig {

    @Bean
    public JournalPersistenceMapper journalPersistenceMapper() {
        return new JournalPersistenceMapper();
    }

    @Bean
    public JournalEntryRepository journalEntryRepository(JournalEntryJpaRepository jpaRepository,
                                                         JournalPersistenceMapper mapper) {
        return new JournalEntryRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public PostJournalForEventUseCase postJournalForEventUseCase(SchemaRepository schemaDomainRepository,
                                                                 JournalEntryRepository journalEntryRepository) {
        return new PostJournalForEventUseCaseImpl(schemaDomainRepository, journalEntryRepository);
    }

    @Bean
    public JournalEntryQueryPort journalEntryQueryPort(JournalEntryJpaRepository jpaRepository,
                                                       JournalPersistenceMapper mapper) {
        return new JournalEntryQueryPortImpl(jpaRepository, mapper);
    }

    @Bean
    public FindJournalEntriesUseCase findJournalEntriesUseCase(JournalEntryQueryPort queryPort,
                                                               PlatformTransactionManager txManager) {
        FindJournalEntriesUseCase pure = new FindJournalEntriesUseCaseImpl(queryPort);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (JournalEntryFilter filter, Pageable pageable) -> tx.execute(status -> pure.execute(filter, pageable));
    }

    @Bean
    public GetJournalEntryDetailUseCase getJournalEntryDetailUseCase(JournalEntryQueryPort queryPort,
                                                                     PlatformTransactionManager txManager) {
        GetJournalEntryDetailUseCase pure = new GetJournalEntryDetailUseCaseImpl(queryPort);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (Long id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public CreateManualJournalUseCase createManualJournalUseCase(JournalEntryRepository repository,
                                                                 CoaPostingValidator coaPostingValidator,
                                                                 CurrencyPostingValidator currencyPostingValidator,
                                                                 PlatformTransactionManager txManager) {
        CreateManualJournalUseCase pure = new CreateManualJournalUseCaseImpl(repository, coaPostingValidator, currencyPostingValidator);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (ManualJournalCommand command) -> tx.execute(status -> pure.execute(command));
    }

    @Bean
    public UpdateManualJournalUseCase updateManualJournalUseCase(JournalEntryRepository repository,
                                                                 CoaPostingValidator coaPostingValidator,
                                                                 CurrencyPostingValidator currencyPostingValidator,
                                                                 PlatformTransactionManager txManager) {
        UpdateManualJournalUseCase pure = new UpdateManualJournalUseCaseImpl(repository, coaPostingValidator, currencyPostingValidator);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (Long id, ManualJournalCommand command) -> tx.execute(status -> pure.execute(id, command));
    }

    @Bean
    public DeleteManualJournalUseCase deleteManualJournalUseCase(JournalEntryRepository repository,
                                                                 PlatformTransactionManager txManager) {
        DeleteManualJournalUseCase pure = new DeleteManualJournalUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (Long id) -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public PostManualJournalUseCase postManualJournalUseCase(JournalEntryRepository repository,
                                                             CoaPostingValidator coaPostingValidator,
                                                             CurrencyPostingValidator currencyPostingValidator,
                                                             EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase,
                                                             PlatformTransactionManager txManager) {
        PostManualJournalUseCase pure = new PostManualJournalUseCaseImpl(
                repository, coaPostingValidator, currencyPostingValidator, ensureOpenPeriodForDateUseCase);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (Long id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public ReverseManualJournalUseCase reverseManualJournalUseCase(JournalEntryRepository repository,
                                                                   EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase,
                                                                   PlatformTransactionManager txManager) {
        ReverseManualJournalUseCase pure = new ReverseManualJournalUseCaseImpl(repository, ensureOpenPeriodForDateUseCase);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (Long id, LocalDate reversalDate) -> tx.execute(status -> pure.execute(id, reversalDate));
    }
}
