package com.solusi.erp.inventory.uom.infrastructure.config;

import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.uom.application.usecase.command.*;
import com.solusi.erp.inventory.uom.application.usecase.query.*;
import com.solusi.erp.inventory.uom.domain.repository.UomRepository;
import com.solusi.erp.inventory.uom.infrastructure.adapter.UomRepositoryImpl;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Composition Root for UnitOfMeasure module.
 * Each use case gets its own TransactionTemplate instance to prevent state mutation bugs.
 */
@Configuration
public class UomConfig {

    @Bean
    public UomRepository uomDomainRepository(
            com.solusi.erp.inventory.repository.UnitOfMeasureRepository jpaRepository,
            UomPersistenceMapper mapper) {
        return new UomRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CreateUomUseCase createUomUseCase(
            UomRepository uomDomainRepository,
            PlatformTransactionManager txManager) {
        CreateUomUseCase pure = new CreateUomUseCaseImpl(uomDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (code, name, type) -> tx.execute(status -> pure.execute(code, name, type));
    }

    @Bean
    public UpdateUomUseCase updateUomUseCase(
            UomRepository repository,
            PlatformTransactionManager txManager) {
        UpdateUomUseCase pure = new UpdateUomUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, type) -> tx.execute(status -> pure.execute(id, name, type));
    }

    @Bean
    public DeleteUomUseCase deleteUomUseCase(
            UomRepository repository,
            PlatformTransactionManager txManager) {
        DeleteUomUseCase pure = new DeleteUomUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public FindUomsUseCase findUomsUseCase(
            UomRepository repository,
            PlatformTransactionManager txManager) {
        FindUomsUseCase pure = new FindUomsUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetUomEditViewUseCase getUomEditViewUseCase(
            UomRepository repository,
            PlatformTransactionManager txManager) {
        GetUomEditViewUseCase pure = new GetUomEditViewUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetUomLookupUseCase getUomLookupUseCase(
            UomRepository repository,
            PlatformTransactionManager txManager) {
        GetUomLookupUseCase pure = new GetUomLookupUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return new GetUomLookupUseCase() {
            @Override
            public com.solusi.erp.core.dto.LookupDto getById(Long id) {
                return tx.execute(status -> pure.getById(id));
            }
            @Override
            public List<com.solusi.erp.core.dto.LookupDto> search(String keyword, int limit) {
                return tx.execute(status -> pure.search(keyword, limit));
            }
            @Override
            public List<com.solusi.erp.core.dto.LookupDto> findByType(UomType type) {
                return tx.execute(status -> pure.findByType(type));
            }
        };
    }
}
