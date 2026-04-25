package com.solusi.erp.master.tax.infrastructure.config;

import com.solusi.erp.master.tax.application.usecase.command.*;
import com.solusi.erp.master.tax.domain.port.TaxLookupProvider;
import com.solusi.erp.master.tax.application.usecase.query.*;
import com.solusi.erp.master.tax.domain.port.TaxInUseChecker;
import com.solusi.erp.master.tax.domain.repository.TaxRepository;
import com.solusi.erp.master.tax.infrastructure.adapter.TaxLookupProviderImpl;
import com.solusi.erp.master.tax.infrastructure.adapter.TaxInUseCheckerImpl;
import com.solusi.erp.master.tax.infrastructure.adapter.TaxRepositoryImpl;
import com.solusi.erp.master.tax.infrastructure.persistence.TaxPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composition Root for Tax module.
 * Each use case gets its own TransactionTemplate instance to prevent state mutation bugs.
 */
@Configuration
public class TaxConfig {

    @Bean
    public TaxRepository taxDomainRepository(
            com.solusi.erp.master.tax.infrastructure.persistence.TaxJpaRepository jpaRepository,
            TaxPersistenceMapper mapper) {
        return new TaxRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CreateTaxUseCase createTaxUseCase(
            TaxRepository taxDomainRepository,
            PlatformTransactionManager txManager) {
        CreateTaxUseCase pure = new CreateTaxUseCaseImpl(taxDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (code, name, rate, note, isSubtract, isActive, calculationMode) ->
                tx.execute(status -> pure.execute(code, name, rate, note, isSubtract, isActive, calculationMode));
    }

    @Bean
    public UpdateTaxUseCase updateTaxUseCase(
            TaxRepository taxDomainRepository,
            PlatformTransactionManager txManager) {
        UpdateTaxUseCase pure = new UpdateTaxUseCaseImpl(taxDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, rate, note, isSubtract, isActive, calculationMode) ->
                tx.execute(status -> pure.execute(id, name, rate, note, isSubtract, isActive, calculationMode));
    }

    @Bean
    public TaxInUseChecker taxInUseChecker() {
        return new TaxInUseCheckerImpl();
    }

    @Bean
    public DeleteTaxUseCase deleteTaxUseCase(
            TaxRepository taxDomainRepository,
            TaxInUseChecker taxInUseChecker,
            PlatformTransactionManager txManager) {
        DeleteTaxUseCase pure = new DeleteTaxUseCaseImpl(taxDomainRepository, taxInUseChecker);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindTaxesUseCase findTaxesUseCase(
            TaxRepository taxDomainRepository,
            PlatformTransactionManager txManager) {
        FindTaxesUseCase pure = new FindTaxesUseCaseImpl(taxDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetTaxEditViewUseCase getTaxEditViewUseCase(
            TaxRepository taxDomainRepository,
            PlatformTransactionManager txManager) {
        GetTaxEditViewUseCase pure = new GetTaxEditViewUseCaseImpl(taxDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetTaxLookupUseCase getTaxLookupUseCase(
            com.solusi.erp.master.tax.infrastructure.persistence.TaxJpaRepository taxJpaRepository,
            PlatformTransactionManager txManager) {
        GetTaxLookupUseCase pure = new GetTaxLookupUseCaseImpl(taxJpaRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return new GetTaxLookupUseCase() {
            @Override
            public java.util.List<com.solusi.erp.core.dto.LookupDto> search(String q, int limit) {
                return tx.execute(status -> pure.search(q, limit));
            }

            @Override
            public com.solusi.erp.core.dto.LookupDto getById(Long id) {
                return tx.execute(status -> pure.getById(id));
            }
        };
    }

    @Bean
    public TaxLookupProvider taxLookupProvider(
            com.solusi.erp.master.tax.infrastructure.persistence.TaxJpaRepository taxJpaRepository) {
        return new TaxLookupProviderImpl(taxJpaRepository);
    }
}

