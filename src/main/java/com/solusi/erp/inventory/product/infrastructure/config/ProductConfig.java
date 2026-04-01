package com.solusi.erp.inventory.product.infrastructure.config;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.product.application.usecase.command.*;
import com.solusi.erp.inventory.product.application.usecase.query.*;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;
import com.solusi.erp.inventory.product.infrastructure.adapter.ProductRepositoryImpl;
import com.solusi.erp.inventory.product.infrastructure.adapter.ProductUomUsageChecker;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.product.infrastructure.adapter.ProductLookupProviderImpl;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductPersistenceMapper;
import com.solusi.erp.inventory.uom.domain.port.UomUsageChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composition Root for Product module.
 */
@Configuration
public class ProductConfig {

    @Bean
    public UomUsageChecker productUomUsageChecker(JpaProductRepository jpaProductRepository) {
        return new ProductUomUsageChecker(jpaProductRepository);
    }

    @Bean
    public ProductRepository productRepository(
            JpaProductRepository jpaRepository,
            ProductPersistenceMapper mapper) {
        return new ProductRepositoryImpl(jpaRepository, mapper);
    }

    // COMMANDS
    @Bean
    public CreateProductUseCase createProductUseCase(
            ProductRepository productRepository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreateProductUseCase pureUseCase = new CreateProductUseCaseImpl(productRepository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (name, barcode, note, categoryId, uomId, brandId, hscode, isActive, isSerialized, minStock, maxStock, weightNet, weightGross, weightUomId, length, width, height, dimensionUomId) ->
            tx.execute(status -> pureUseCase.execute(name, barcode, note, categoryId, uomId, brandId, hscode, isActive, isSerialized, minStock, maxStock, weightNet, weightGross, weightUomId, length, width, height, dimensionUomId));
    }

    @Bean
    public UpdateProductUseCase updateProductUseCase(
            ProductRepository productRepository,
            PlatformTransactionManager txManager) {
        UpdateProductUseCase pureUseCase = new UpdateProductUseCaseImpl(productRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, barcode, note, categoryId, uomId, brandId, hscode, isActive, isSerialized, minStock, maxStock, weightNet, weightGross, weightUomId, length, width, height, dimensionUomId) ->
            tx.execute(status -> pureUseCase.execute(id, name, barcode, note, categoryId, uomId, brandId, hscode, isActive, isSerialized, minStock, maxStock, weightNet, weightGross, weightUomId, length, width, height, dimensionUomId));
    }

    @Bean
    public DeleteProductUseCase deleteProductUseCase(
            ProductRepository productRepository,
            PlatformTransactionManager txManager) {
        DeleteProductUseCase pureUseCase = new DeleteProductUseCaseImpl(productRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pureUseCase.execute(id));
    }

    // QUERIES
    @Bean
    public FindProductsUseCase findProductsUseCase(
            ProductRepository productRepository,
            PlatformTransactionManager txManager) {
        FindProductsUseCase pureUseCase = new FindProductsUseCaseImpl(productRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pureUseCase.execute(keyword, pageable));
    }

    @Bean
    public GetProductUseCase getProductUseCase(
            ProductRepository productRepository,
            PlatformTransactionManager txManager) {
        GetProductUseCase pureUseCase = new GetProductUseCaseImpl(productRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pureUseCase.execute(id));
    }

    @Bean
    public GetProductEditViewUseCase getProductEditViewUseCase(
            ProductRepository productRepository,
            PlatformTransactionManager txManager) {
        GetProductEditViewUseCase pureUseCase = new GetProductEditViewUseCaseImpl(productRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pureUseCase.execute(id));
    }

    @Bean
    public GetProductLookupUseCase getProductLookupUseCase(
            ProductRepository productRepository,
            PlatformTransactionManager txManager) {
        GetProductLookupUseCase pureUseCase = new GetProductLookupUseCaseImpl(productRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return new GetProductLookupUseCase() {
            @Override
            public com.solusi.erp.core.dto.LookupDto getById(Long id) {
                return tx.execute(status -> pureUseCase.getById(id));
            }

            @Override
            public java.util.List<com.solusi.erp.core.dto.LookupDto> search(String keyword, int limit) {
                return tx.execute(status -> pureUseCase.search(keyword, limit));
            }
        };
    }

    @Bean
    public ProductLookupProvider productLookupProvider(JpaProductRepository jpaProductRepository) {
        return new ProductLookupProviderImpl(jpaProductRepository);
    }
}
