package com.solusi.erp.master.party.infrastructure.config;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.master.party.application.usecase.command.*;
import com.solusi.erp.master.party.application.usecase.query.*;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyInUseChecker;
import com.solusi.erp.master.party.domain.repository.PartyRepository;
import com.solusi.erp.master.party.infrastructure.adapter.PartyInUseCheckerImpl;
import com.solusi.erp.master.party.infrastructure.adapter.PartyLookupProviderImpl;
import com.solusi.erp.master.party.infrastructure.adapter.PartyRepositoryImpl;
import com.solusi.erp.master.party.infrastructure.persistence.PartyPersistenceMapper;
import com.solusi.erp.master.geographic.infrastructure.persistence.GeographicJpaRepository;
import com.solusi.erp.master.party.infrastructure.persistence.PartyIdentificationTypeJpaRepository;
import com.solusi.erp.master.party.infrastructure.persistence.PartyJpaRepository;
import com.solusi.erp.master.partyroletype.infrastructure.persistence.PartyRoleTypeJpaRepository;
import com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccountJpaRepository;
import com.solusi.erp.master.party.domain.model.Party;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Configuration
public class PartyConfig {

    @Bean
    public PartyRepository partyDomainRepository(
            com.solusi.erp.master.party.infrastructure.persistence.PartyJpaRepository partyRepository,
            PartyRoleTypeJpaRepository partyRoleTypeRepository,
            GeographicJpaRepository geographicRepository,
            PartyIdentificationTypeJpaRepository partyIdentificationTypeRepository,
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
    public PartyInUseChecker partyInUseChecker(BankAccountJpaRepository bankAccountJpaRepository) {
        return new PartyInUseCheckerImpl(bankAccountJpaRepository);
    }

    @Bean
    public DeletePartyUseCase deletePartyUseCase(
            PartyRepository partyDomainRepository,
            PartyInUseChecker partyInUseChecker,
            PlatformTransactionManager txManager) {
        DeletePartyUseCase pure = new DeletePartyUseCaseImpl(partyDomainRepository, partyInUseChecker);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.execute(status -> pure.execute(id));
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
    public GetPartyReferenceUseCase getPartyReferenceUseCase(
            PartyRepository partyDomainRepository,
            PlatformTransactionManager txManager) {
        GetPartyReferenceUseCase pure = new GetPartyReferenceUseCaseImpl(partyDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindPartiesForLookupUseCase findPartiesForLookupUseCase(
            PartyRepository partyDomainRepository,
            PlatformTransactionManager txManager) {
        FindPartiesForLookupUseCaseImpl pure = new FindPartiesForLookupUseCaseImpl(partyDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return new FindPartiesForLookupUseCase() {
            @Override
            public List<Party> execute(String keyword) {
                return tx.execute(status -> pure.execute(keyword));
            }

            @Override
            public List<Party> executeByRoleType(String keyword, String roleTypeCode, Long excludePartyId) {
                return tx.execute(status -> pure.executeByRoleType(keyword, roleTypeCode, excludePartyId));
            }
        };
    }

    @Bean
    public FindPartiesAvailableForUserUseCase findPartiesAvailableForUserUseCase(
            PartyRepository partyDomainRepository,
            PlatformTransactionManager txManager) {
        FindPartiesAvailableForUserUseCase pure = new FindPartiesAvailableForUserUseCaseImpl(partyDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, excludePartyId) -> tx.execute(status -> pure.execute(keyword, excludePartyId));
    }

    @Bean
    public PartyLookupProvider partyLookupProvider(
            PartyJpaRepository partyJpaRepository,
            MessageSource messageSource) {
        return new PartyLookupProviderImpl(partyJpaRepository, messageSource);
    }
}


