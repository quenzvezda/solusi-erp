package com.solusi.erp.inventory.brand.infrastructure.config;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.brand.application.usecase.command.*;
import com.solusi.erp.inventory.brand.application.usecase.query.*;
import com.solusi.erp.inventory.brand.domain.port.BrandInUseChecker;
import com.solusi.erp.inventory.brand.domain.repository.BrandRepository;
import com.solusi.erp.inventory.brand.infrastructure.adapter.BrandInUseCheckerImpl;
import com.solusi.erp.inventory.brand.infrastructure.adapter.BrandRepositoryImpl;
import com.solusi.erp.inventory.brand.infrastructure.persistence.BrandJpaRepository;
import com.solusi.erp.inventory.brand.infrastructure.persistence.BrandPersistenceMapper;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composition Root for Brand module.
 * Each use case gets its own TransactionTemplate instance to prevent state mutation bugs.
 */
@Configuration
public class BrandConfig {

    @Bean
    public BrandRepository brandDomainRepository(
            BrandJpaRepository jpaRepository,
            BrandPersistenceMapper mapper) {
        return new BrandRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CreateBrandUseCase createBrandUseCase(
            BrandRepository brandDomainRepository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreateBrandUseCase pure = new CreateBrandUseCaseImpl(brandDomainRepository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (name, note) -> tx.execute(status -> pure.execute(name, note));
    }

    @Bean
    public UpdateBrandUseCase updateBrandUseCase(
            BrandRepository repository,
            PlatformTransactionManager txManager) {
        UpdateBrandUseCase pure = new UpdateBrandUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, note) -> tx.execute(status -> pure.execute(id, name, note));
    }

    @Bean
    public BrandInUseChecker brandInUseChecker(JpaProductRepository jpaProductRepository) {
        return new BrandInUseCheckerImpl(jpaProductRepository);
    }

    @Bean
    public DeleteBrandUseCase deleteBrandUseCase(
            BrandRepository repository,
            BrandInUseChecker brandInUseChecker,
            PlatformTransactionManager txManager) {
        DeleteBrandUseCase pure = new DeleteBrandUseCaseImpl(repository, brandInUseChecker);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public FindBrandsUseCase findBrandsUseCase(
            BrandRepository repository,
            PlatformTransactionManager txManager) {
        FindBrandsUseCase pure = new FindBrandsUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetBrandEditViewUseCase getBrandEditViewUseCase(
            BrandRepository repository,
            PlatformTransactionManager txManager) {
        GetBrandEditViewUseCase pure = new GetBrandEditViewUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetBrandLookupUseCase getBrandLookupUseCase(
            BrandRepository repository,
            PlatformTransactionManager txManager) {
        GetBrandLookupUseCase pure = new GetBrandLookupUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return new GetBrandLookupUseCase() {
            @Override
            public com.solusi.erp.core.dto.LookupDto getById(Long id) {
                return tx.execute(status -> pure.getById(id));
            }
            @Override
            public java.util.List<com.solusi.erp.core.dto.LookupDto> search(String keyword, int limit) {
                return tx.execute(status -> pure.search(keyword, limit));
            }
        };
    }
}
