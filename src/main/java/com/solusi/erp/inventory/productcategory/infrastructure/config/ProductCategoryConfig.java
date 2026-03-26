package com.solusi.erp.inventory.productcategory.infrastructure.config;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.productcategory.application.usecase.command.*;
import com.solusi.erp.inventory.productcategory.application.usecase.query.*;
import com.solusi.erp.inventory.productcategory.domain.repository.ProductCategoryRepository;
import com.solusi.erp.inventory.productcategory.infrastructure.adapter.ProductCategoryRepositoryImpl;
import com.solusi.erp.inventory.productcategory.infrastructure.persistence.ProductCategoryPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class ProductCategoryConfig {

    @Bean
    public ProductCategoryRepository productCategoryDomainRepository(
            com.solusi.erp.inventory.repository.ProductCategoryRepository jpaRepository,
            ProductCategoryPersistenceMapper mapper) {
        return new ProductCategoryRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CreateProductCategoryUseCase createProductCategoryUseCase(
            ProductCategoryRepository repository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreateProductCategoryUseCase pure = new CreateProductCategoryUseCaseImpl(repository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (name, type, note) -> tx.execute(status -> pure.execute(name, type, note));
    }

    @Bean
    public UpdateProductCategoryUseCase updateProductCategoryUseCase(
            ProductCategoryRepository repository,
            PlatformTransactionManager txManager) {
        UpdateProductCategoryUseCase pure = new UpdateProductCategoryUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, type, note) -> tx.execute(status -> pure.execute(id, name, type, note));
    }

    @Bean
    public DeleteProductCategoryUseCase deleteProductCategoryUseCase(
            ProductCategoryRepository repository,
            PlatformTransactionManager txManager) {
        DeleteProductCategoryUseCase pure = new DeleteProductCategoryUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public FindProductCategoriesUseCase findProductCategoriesUseCase(
            ProductCategoryRepository repository,
            PlatformTransactionManager txManager) {
        FindProductCategoriesUseCase pure = new FindProductCategoriesUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetProductCategoryUseCase getProductCategoryUseCase(
            ProductCategoryRepository repository,
            PlatformTransactionManager txManager) {
        GetProductCategoryUseCase pure = new GetProductCategoryUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetProductCategoryEditViewUseCase getProductCategoryEditViewUseCase(
            ProductCategoryRepository repository,
            PlatformTransactionManager txManager) {
        GetProductCategoryEditViewUseCase pure = new GetProductCategoryEditViewUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetProductCategoryLookupUseCase getProductCategoryLookupUseCase(
            ProductCategoryRepository repository,
            PlatformTransactionManager txManager) {
        GetProductCategoryLookupUseCase pure = new GetProductCategoryLookupUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return new GetProductCategoryLookupUseCase() {
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
