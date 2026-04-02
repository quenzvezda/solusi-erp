package com.solusi.erp.master.geographic.infrastructure.config;

import com.solusi.erp.master.geographic.application.usecase.command.*;
import com.solusi.erp.master.geographic.application.usecase.query.*;
import com.solusi.erp.master.geographic.domain.port.GeographicLookupProvider;
import com.solusi.erp.master.geographic.domain.repository.GeographicRepository;
import com.solusi.erp.master.geographic.infrastructure.adapter.GeographicLookupProviderImpl;
import com.solusi.erp.master.geographic.domain.port.GeographicInUseChecker;
import com.solusi.erp.master.geographic.infrastructure.adapter.GeographicInUseCheckerImpl;
import com.solusi.erp.master.geographic.infrastructure.adapter.GeographicRepositoryImpl;
import com.solusi.erp.master.geographic.infrastructure.persistence.GeographicJpaRepository;
import com.solusi.erp.master.geographic.infrastructure.persistence.GeographicPersistenceMapper;
import com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccountJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composition Root for Geographic module.
 * Each use case gets its own TransactionTemplate instance to prevent state mutation bugs.
 */
@Configuration
public class GeographicConfig {

    @Bean
    public GeographicRepository geographicDomainRepository(
            com.solusi.erp.master.geographic.infrastructure.persistence.GeographicJpaRepository jpaRepository,
            GeographicPersistenceMapper mapper) {
        return new GeographicRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CreateGeographicUseCase createGeographicUseCase(
            GeographicRepository geographicDomainRepository,
            PlatformTransactionManager txManager) {
        CreateGeographicUseCase pure = new CreateGeographicUseCaseImpl(geographicDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (code, name, type, parentId, parentName, isActive) ->
                tx.execute(status -> pure.execute(code, name, type, parentId, parentName, isActive));
    }

    @Bean
    public UpdateGeographicUseCase updateGeographicUseCase(
            GeographicRepository geographicDomainRepository,
            PlatformTransactionManager txManager) {
        UpdateGeographicUseCase pure = new UpdateGeographicUseCaseImpl(geographicDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, type, parentId, parentName, isActive) ->
                tx.execute(status -> pure.execute(id, name, type, parentId, parentName, isActive));
    }

    @Bean
    public GeographicInUseChecker geographicInUseChecker(
            GeographicJpaRepository geographicJpaRepository,
            BankAccountJpaRepository bankAccountJpaRepository) {
        return new GeographicInUseCheckerImpl(geographicJpaRepository, bankAccountJpaRepository);
    }

    @Bean
    public DeleteGeographicUseCase deleteGeographicUseCase(
            GeographicRepository geographicDomainRepository,
            GeographicInUseChecker geographicInUseChecker,
            PlatformTransactionManager txManager) {
        DeleteGeographicUseCase pure = new DeleteGeographicUseCaseImpl(geographicDomainRepository, geographicInUseChecker);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindGeographicsUseCase findGeographicsUseCase(
            GeographicRepository geographicDomainRepository,
            PlatformTransactionManager txManager) {
        FindGeographicsUseCase pure = new FindGeographicsUseCaseImpl(geographicDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, parentId, pageable) ->
                tx.execute(status -> pure.execute(keyword, parentId, pageable));
    }

    @Bean
    public GetGeographicEditViewUseCase getGeographicEditViewUseCase(
            GeographicRepository geographicDomainRepository,
            PlatformTransactionManager txManager) {
        GetGeographicEditViewUseCase pure = new GetGeographicEditViewUseCaseImpl(geographicDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindCountriesUseCase findCountriesUseCase(
            GeographicRepository geographicDomainRepository,
            PlatformTransactionManager txManager) {
        FindCountriesUseCase pure = new FindCountriesUseCaseImpl(geographicDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, limit) -> tx.execute(status -> pure.execute(keyword, limit));
    }

    @Bean
    public FindProvincesByCountryUseCase findProvincesByCountryUseCase(
            GeographicRepository geographicDomainRepository,
            PlatformTransactionManager txManager) {
        FindProvincesByCountryUseCase pure = new FindProvincesByCountryUseCaseImpl(geographicDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (countryId, keyword, limit) -> tx.execute(status -> pure.execute(countryId, keyword, limit));
    }

    @Bean
    public FindCitiesByProvinceUseCase findCitiesByProvinceUseCase(
            GeographicRepository geographicDomainRepository,
            PlatformTransactionManager txManager) {
        FindCitiesByProvinceUseCase pure = new FindCitiesByProvinceUseCaseImpl(geographicDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (provinceId, keyword, limit) -> tx.execute(status -> pure.execute(provinceId, keyword, limit));
    }

    @Bean
    public GeographicLookupProvider geographicLookupProvider(
            GeographicJpaRepository geographicJpaRepository) {
        return new GeographicLookupProviderImpl(geographicJpaRepository);
    }
}

