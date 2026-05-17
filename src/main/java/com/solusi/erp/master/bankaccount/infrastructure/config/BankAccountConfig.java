package com.solusi.erp.master.bankaccount.infrastructure.config;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.master.bankaccount.application.usecase.command.*;
import com.solusi.erp.master.bankaccount.application.usecase.query.*;
import com.solusi.erp.master.bankaccount.domain.port.BankAccountInUseChecker;
import com.solusi.erp.master.bankaccount.domain.port.BankAccountLookupProvider;
import com.solusi.erp.master.bankaccount.infrastructure.adapter.BankAccountInUseCheckerImpl;
import com.solusi.erp.master.bankaccount.infrastructure.adapter.BankAccountLookupProviderImpl;
import com.solusi.erp.master.bankaccount.domain.repository.BankAccountRepository;
import com.solusi.erp.master.bankaccount.infrastructure.adapter.BankAccountRepositoryImpl;
import com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccountPersistenceMapper;
import com.solusi.erp.master.geographic.infrastructure.persistence.GeographicJpaRepository;
import com.solusi.erp.master.party.infrastructure.persistence.PartyJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composition Root for BankAccount module.
 * Each use case gets its own TransactionTemplate instance to prevent state mutation bugs.
 */
@Configuration
public class BankAccountConfig {

    @Bean
    public BankAccountRepository bankAccountDomainRepository(
            com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccountJpaRepository jpaRepository,
            BankAccountPersistenceMapper mapper,
            GeographicJpaRepository geographicRepository,
            PartyJpaRepository partyRepository) {
        return new BankAccountRepositoryImpl(jpaRepository, mapper, geographicRepository, partyRepository);
    }

    @Bean
    public CreateBankAccountUseCase createBankAccountUseCase(
            BankAccountRepository bankAccountDomainRepository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreateBankAccountUseCase pure = new CreateBankAccountUseCaseImpl(bankAccountDomainRepository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (bankName, branch, accountName, accountNo, accountType, note, cityId, partyId, isActive, currencyId, coaId) ->
                tx.execute(status -> pure.execute(bankName, branch, accountName, accountNo, accountType, note, cityId, partyId, isActive, currencyId, coaId));
    }

    @Bean
    public UpdateBankAccountUseCase updateBankAccountUseCase(
            BankAccountRepository bankAccountDomainRepository,
            PlatformTransactionManager txManager) {
        UpdateBankAccountUseCase pure = new UpdateBankAccountUseCaseImpl(bankAccountDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, bankName, branch, accountName, accountNo, accountType, note, cityId, partyId, isActive, currencyId, coaId) ->
                tx.execute(status -> pure.execute(id, bankName, branch, accountName, accountNo, accountType, note, cityId, partyId, isActive, currencyId, coaId));
    }

    @Bean
    public BankAccountInUseChecker bankAccountInUseChecker() {
        return new BankAccountInUseCheckerImpl();
    }

    @Bean
    public DeleteBankAccountUseCase deleteBankAccountUseCase(
            BankAccountRepository bankAccountDomainRepository,
            BankAccountInUseChecker bankAccountInUseChecker,
            PlatformTransactionManager txManager) {
        DeleteBankAccountUseCase pure = new DeleteBankAccountUseCaseImpl(bankAccountDomainRepository, bankAccountInUseChecker);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindBankAccountsUseCase findBankAccountsUseCase(
            BankAccountRepository bankAccountDomainRepository,
            PlatformTransactionManager txManager) {
        FindBankAccountsUseCase pure = new FindBankAccountsUseCaseImpl(bankAccountDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetBankAccountEditViewUseCase getBankAccountEditViewUseCase(
            BankAccountRepository bankAccountDomainRepository,
            PlatformTransactionManager txManager) {
        GetBankAccountEditViewUseCase pure = new GetBankAccountEditViewUseCaseImpl(bankAccountDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public BankAccountLookupProvider bankAccountLookupProvider(
            com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccountJpaRepository jpaRepository) {
        return new BankAccountLookupProviderImpl(jpaRepository);
    }
}


