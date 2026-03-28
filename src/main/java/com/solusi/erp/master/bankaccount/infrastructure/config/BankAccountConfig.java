package com.solusi.erp.master.bankaccount.infrastructure.config;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.master.bankaccount.application.usecase.command.*;
import com.solusi.erp.master.bankaccount.application.usecase.query.*;
import com.solusi.erp.master.bankaccount.domain.repository.BankAccountRepository;
import com.solusi.erp.master.bankaccount.infrastructure.adapter.BankAccountRepositoryImpl;
import com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccountPersistenceMapper;
import com.solusi.erp.master.repository.GeographicRepository;
import com.solusi.erp.master.repository.PartyRepository;
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
            com.solusi.erp.master.repository.BankAccountRepository jpaRepository,
            BankAccountPersistenceMapper mapper,
            GeographicRepository geographicRepository,
            PartyRepository partyRepository) {
        return new BankAccountRepositoryImpl(jpaRepository, mapper, geographicRepository, partyRepository);
    }

    @Bean
    public CreateBankAccountUseCase createBankAccountUseCase(
            BankAccountRepository bankAccountDomainRepository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreateBankAccountUseCase pure = new CreateBankAccountUseCaseImpl(bankAccountDomainRepository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (bankName, branch, accountName, accountNo, accountType, note, cityId, partyId, isActive) ->
                tx.execute(status -> pure.execute(bankName, branch, accountName, accountNo, accountType, note, cityId, partyId, isActive));
    }

    @Bean
    public UpdateBankAccountUseCase updateBankAccountUseCase(
            BankAccountRepository bankAccountDomainRepository,
            PlatformTransactionManager txManager) {
        UpdateBankAccountUseCase pure = new UpdateBankAccountUseCaseImpl(bankAccountDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, bankName, branch, accountName, accountNo, accountType, note, cityId, partyId, isActive) ->
                tx.execute(status -> pure.execute(id, bankName, branch, accountName, accountNo, accountType, note, cityId, partyId, isActive));
    }

    @Bean
    public DeleteBankAccountUseCase deleteBankAccountUseCase(
            BankAccountRepository bankAccountDomainRepository,
            PlatformTransactionManager txManager) {
        DeleteBankAccountUseCase pure = new DeleteBankAccountUseCaseImpl(bankAccountDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pure.execute(id));
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
}
