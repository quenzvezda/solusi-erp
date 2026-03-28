package com.solusi.erp.master.partyroletype.infrastructure.config;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.master.partyroletype.application.usecase.command.*;
import com.solusi.erp.master.partyroletype.application.usecase.query.*;
import com.solusi.erp.master.partyroletype.domain.repository.PartyRoleTypeRepository;
import com.solusi.erp.master.partyroletype.infrastructure.adapter.PartyRoleTypeRepositoryImpl;
import com.solusi.erp.master.partyroletype.infrastructure.persistence.PartyRoleTypePersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composition Root for PartyRoleType module.
 * Each use case gets its own TransactionTemplate instance to prevent state mutation bugs.
 */
@Configuration
public class PartyRoleTypeConfig {

    @Bean
    public PartyRoleTypeRepository partyRoleTypeDomainRepository(
            com.solusi.erp.master.partyroletype.infrastructure.persistence.PartyRoleTypeJpaRepository jpaRepository,
            PartyRoleTypePersistenceMapper mapper) {
        return new PartyRoleTypeRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CreatePartyRoleTypeUseCase createPartyRoleTypeUseCase(
            PartyRoleTypeRepository partyRoleTypeDomainRepository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreatePartyRoleTypeUseCase pure = new CreatePartyRoleTypeUseCaseImpl(
                partyRoleTypeDomainRepository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (name, note, isActive) -> tx.execute(status -> pure.execute(name, note, isActive));
    }

    @Bean
    public UpdatePartyRoleTypeUseCase updatePartyRoleTypeUseCase(
            PartyRoleTypeRepository partyRoleTypeDomainRepository,
            PlatformTransactionManager txManager) {
        UpdatePartyRoleTypeUseCase pure = new UpdatePartyRoleTypeUseCaseImpl(partyRoleTypeDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, note, isActive) -> tx.execute(status -> pure.execute(id, name, note, isActive));
    }

    @Bean
    public DeletePartyRoleTypeUseCase deletePartyRoleTypeUseCase(
            PartyRoleTypeRepository partyRoleTypeDomainRepository,
            PlatformTransactionManager txManager) {
        DeletePartyRoleTypeUseCase pure = new DeletePartyRoleTypeUseCaseImpl(partyRoleTypeDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public FindPartyRoleTypesUseCase findPartyRoleTypesUseCase(
            PartyRoleTypeRepository partyRoleTypeDomainRepository,
            PlatformTransactionManager txManager) {
        FindPartyRoleTypesUseCase pure = new FindPartyRoleTypesUseCaseImpl(partyRoleTypeDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetPartyRoleTypeEditViewUseCase getPartyRoleTypeEditViewUseCase(
            PartyRoleTypeRepository partyRoleTypeDomainRepository,
            PlatformTransactionManager txManager) {
        GetPartyRoleTypeEditViewUseCase pure = new GetPartyRoleTypeEditViewUseCaseImpl(partyRoleTypeDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }
}

