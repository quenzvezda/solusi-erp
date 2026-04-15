package com.solusi.erp.master.currency.infrastructure.config;

import com.solusi.erp.master.currency.application.usecase.command.*;
import com.solusi.erp.master.currency.application.usecase.query.*;
import com.solusi.erp.master.currency.domain.port.CurrencyInUseChecker;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;
import com.solusi.erp.master.currency.infrastructure.adapter.CurrencyInUseCheckerImpl;
import com.solusi.erp.master.currency.infrastructure.adapter.CurrencyLookupProviderImpl;
import com.solusi.erp.master.currency.infrastructure.adapter.CurrencyRepositoryImpl;
import com.solusi.erp.master.currency.infrastructure.persistence.CurrencyPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composition Root for Currency module.
 * Each use case gets its own TransactionTemplate instance to prevent state mutation bugs.
 */
@Configuration
public class CurrencyConfig {

    @Bean
    public CurrencyRepository currencyDomainRepository(
            com.solusi.erp.master.currency.infrastructure.persistence.CurrencyJpaRepository jpaRepository,
            CurrencyPersistenceMapper mapper) {
        return new CurrencyRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CreateCurrencyUseCase createCurrencyUseCase(
            CurrencyRepository currencyDomainRepository,
            PlatformTransactionManager txManager) {
        CreateCurrencyUseCase pure = new CreateCurrencyUseCaseImpl(currencyDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (symbol, alias, name, note, isDefault, isActive) ->
                tx.execute(status -> pure.execute(symbol, alias, name, note, isDefault, isActive));
    }

    @Bean
    public UpdateCurrencyUseCase updateCurrencyUseCase(
            CurrencyRepository currencyDomainRepository,
            PlatformTransactionManager txManager) {
        UpdateCurrencyUseCase pure = new UpdateCurrencyUseCaseImpl(currencyDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, symbol, name, note, isDefault, isActive) ->
                tx.execute(status -> pure.execute(id, symbol, name, note, isDefault, isActive));
    }

    @Bean
    public CurrencyInUseChecker currencyInUseChecker() {
        return new CurrencyInUseCheckerImpl();
    }

    @Bean
    public DeleteCurrencyUseCase deleteCurrencyUseCase(
            CurrencyRepository currencyDomainRepository,
            CurrencyInUseChecker currencyInUseChecker,
            PlatformTransactionManager txManager) {
        DeleteCurrencyUseCase pure = new DeleteCurrencyUseCaseImpl(currencyDomainRepository, currencyInUseChecker);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindCurrenciesUseCase findCurrenciesUseCase(
            CurrencyRepository currencyDomainRepository,
            PlatformTransactionManager txManager) {
        FindCurrenciesUseCase pure = new FindCurrenciesUseCaseImpl(currencyDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetCurrencyEditViewUseCase getCurrencyEditViewUseCase(
            CurrencyRepository currencyDomainRepository,
            PlatformTransactionManager txManager) {
        GetCurrencyEditViewUseCase pure = new GetCurrencyEditViewUseCaseImpl(currencyDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindActiveCurrenciesUseCase findActiveCurrenciesUseCase(
            CurrencyRepository currencyDomainRepository,
            PlatformTransactionManager txManager) {
        FindActiveCurrenciesUseCase pure = new FindActiveCurrenciesUseCaseImpl(currencyDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return () -> tx.execute(status -> pure.execute());
    }

    @Bean
    public GetDefaultCurrencyUseCase getDefaultCurrencyUseCase(
            CurrencyRepository currencyDomainRepository,
            PlatformTransactionManager txManager) {
        GetDefaultCurrencyUseCase pure = new GetDefaultCurrencyUseCaseImpl(currencyDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return () -> tx.execute(status -> pure.execute());
    }

    @Bean
    public CurrencyLookupProvider currencyLookupProvider(
            com.solusi.erp.master.currency.infrastructure.persistence.CurrencyJpaRepository currencyJpaRepository) {
        return new CurrencyLookupProviderImpl(currencyJpaRepository);
    }
}

