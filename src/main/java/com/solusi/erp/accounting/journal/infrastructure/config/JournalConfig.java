package com.solusi.erp.accounting.journal.infrastructure.config;

import com.solusi.erp.accounting.journal.application.policy.GoodsReceiptJournalPolicy;
import com.solusi.erp.accounting.journal.application.policy.JournalPolicyResolver;
import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCaseImpl;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.journal.infrastructure.adapter.JournalEntryRepositoryImpl;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalEntryJpaRepository;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalPersistenceMapper;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
    public JournalPolicyResolver journalPolicyResolver() {
        return new JournalPolicyResolver(List.of(new GoodsReceiptJournalPolicy()));
    }

    @Bean
    public PostJournalForEventUseCase postJournalForEventUseCase(SchemaRepository schemaDomainRepository,
                                                                  JournalEntryRepository journalEntryRepository,
                                                                  JournalPolicyResolver journalPolicyResolver) {
        return new PostJournalForEventUseCaseImpl(
                schemaDomainRepository, journalEntryRepository, journalPolicyResolver);
    }

    @Bean
    public com.solusi.erp.accounting.journal.application.usecase.query.FindJournalEntriesUseCase findJournalEntriesUseCase(com.solusi.erp.accounting.journal.domain.port.JournalEntryQueryPort queryPort) {
        return new com.solusi.erp.accounting.journal.application.usecase.query.FindJournalEntriesUseCaseImpl(queryPort);
    }

    @Bean
    public com.solusi.erp.accounting.journal.application.usecase.query.GetJournalEntryDetailUseCase getJournalEntryDetailUseCase(com.solusi.erp.accounting.journal.domain.port.JournalEntryQueryPort queryPort) {
        return new com.solusi.erp.accounting.journal.application.usecase.query.GetJournalEntryDetailUseCaseImpl(queryPort);
    }
}
