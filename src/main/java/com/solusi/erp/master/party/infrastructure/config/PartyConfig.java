package com.solusi.erp.master.party.infrastructure.config;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.master.party.application.usecase.command.*;
import com.solusi.erp.master.party.application.usecase.query.*;
import com.solusi.erp.master.party.domain.repository.PartyRepository;
import com.solusi.erp.master.party.infrastructure.adapter.PartyRepositoryImpl;
import com.solusi.erp.master.party.infrastructure.persistence.PartyPersistenceMapper;
import com.solusi.erp.master.repository.GeographicRepository;
import com.solusi.erp.master.repository.PartyIdentificationTypeRepository;
import com.solusi.erp.master.repository.PartyRoleTypeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class PartyConfig {

    @Bean
    public PartyRepository partyDomainRepository(
            com.solusi.erp.master.repository.PartyRepository partyRepository,
            PartyRoleTypeRepository partyRoleTypeRepository,
            GeographicRepository geographicRepository,
            PartyIdentificationTypeRepository partyIdentificationTypeRepository,
            PartyPersistenceMapper partyPersistenceMapper) {
        return new PartyRepositoryImpl(
                partyRepository,
                partyRoleTypeRepository,
                geographicRepository,
                partyIdentificationTypeRepository,
                partyPersistenceMapper);
    }

    @Bean
    public CreatePartyUseCase createPartyUseCase(
            PartyRepository partyDomainRepository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreatePartyUseCase pure = new CreatePartyUseCaseImpl(partyDomainRepository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (name, salutation, type, notes, isActive, email, phone, contacts, addresses, identifications, roleIds) ->
                tx.execute(status -> pure.execute(name, salutation, type, notes, isActive, email, phone,
                        contacts, addresses, identifications, roleIds));
    }

    @Bean
    public UpdatePartyUseCase updatePartyUseCase(
            PartyRepository partyDomainRepository,
            PlatformTransactionManager txManager) {
        UpdatePartyUseCase pure = new UpdatePartyUseCaseImpl(partyDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, salutation, type, notes, isActive, email, phone, contacts, addresses, identifications, roleIds) ->
                tx.execute(status -> pure.execute(id, name, salutation, type, notes, isActive, email, phone,
                        contacts, addresses, identifications, roleIds));
    }

    @Bean
    public DeletePartyUseCase deletePartyUseCase(
            PartyRepository partyDomainRepository,
            PlatformTransactionManager txManager) {
        DeletePartyUseCase pure = new DeletePartyUseCaseImpl(partyDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public FindPartiesUseCase findPartiesUseCase(
            PartyRepository partyDomainRepository,
            PlatformTransactionManager txManager) {
        FindPartiesUseCase pure = new FindPartiesUseCaseImpl(partyDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetPartyEditViewUseCase getPartyEditViewUseCase(
            PartyRepository partyDomainRepository,
            PlatformTransactionManager txManager) {
        GetPartyEditViewUseCase pure = new GetPartyEditViewUseCaseImpl(partyDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindPartiesForLookupUseCase findPartiesForLookupUseCase(
            PartyRepository partyDomainRepository,
            PlatformTransactionManager txManager) {
        FindPartiesForLookupUseCase pure = new FindPartiesForLookupUseCaseImpl(partyDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword) -> tx.execute(status -> pure.execute(keyword));
    }
}
