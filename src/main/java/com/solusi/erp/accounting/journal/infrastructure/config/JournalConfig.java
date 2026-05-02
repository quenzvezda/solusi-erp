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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
                                                                  JournalPolicyResolver journalPolicyResolver,
                                                                  PlatformTransactionManager txManager) {
        PostJournalForEventUseCase pure = new PostJournalForEventUseCaseImpl(
                schemaDomainRepository, journalEntryRepository, journalPolicyResolver);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return command -> tx.execute(status -> { pure.execute(command); return null; });
    }
}
