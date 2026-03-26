package com.solusi.erp.inventory.product.infrastructure.config;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.product.application.usecase.command.*;
import com.solusi.erp.inventory.product.application.usecase.query.*;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;
import com.solusi.erp.inventory.product.infrastructure.adapter.ProductRepositoryImpl;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composition Root for Product module.
 */
@Configuration
public class ProductConfig {

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
            TransactionTemplate transactionTemplate) {
        CreateProductUseCase pureUseCase = new CreateProductUseCaseImpl(productRepository, sequenceGeneratorService);
        return (name, barcode, note, categoryId, uomId, brandId, hscode, isActive, isSerialized, minStock, maxStock, weightNet, weightGross, weightUomId, length, width, height, dimensionUomId) ->
            transactionTemplate.execute(status -> pureUseCase.execute(name, barcode, note, categoryId, uomId, brandId, hscode, isActive, isSerialized, minStock, maxStock, weightNet, weightGross, weightUomId, length, width, height, dimensionUomId));
    }

    @Bean
    public UpdateProductUseCase updateProductUseCase(
            ProductRepository productRepository,
            TransactionTemplate transactionTemplate) {
        UpdateProductUseCase pureUseCase = new UpdateProductUseCaseImpl(productRepository);
        return (id, name, barcode, note, categoryId, uomId, brandId, hscode, isActive, isSerialized, minStock, maxStock, weightNet, weightGross, weightUomId, length, width, height, dimensionUomId) ->
            transactionTemplate.execute(status -> pureUseCase.execute(id, name, barcode, note, categoryId, uomId, brandId, hscode, isActive, isSerialized, minStock, maxStock, weightNet, weightGross, weightUomId, length, width, height, dimensionUomId));
    }

    @Bean
    public DeleteProductUseCase deleteProductUseCase(
            ProductRepository productRepository,
            TransactionTemplate transactionTemplate) {
        DeleteProductUseCase pureUseCase = new DeleteProductUseCaseImpl(productRepository);
        return (id) -> transactionTemplate.executeWithoutResult(status -> pureUseCase.execute(id));
    }

    // QUERIES
    @Bean
    public FindProductsUseCase findProductsUseCase(
            ProductRepository productRepository,
            TransactionTemplate transactionTemplate) {
        FindProductsUseCase pureUseCase = new FindProductsUseCaseImpl(productRepository);
        return (keyword, pageable) -> {
            transactionTemplate.setReadOnly(true);
            return transactionTemplate.execute(status -> pureUseCase.execute(keyword, pageable));
        };
    }

    @Bean
    public GetProductUseCase getProductUseCase(
            ProductRepository productRepository,
            TransactionTemplate transactionTemplate) {
        GetProductUseCase pureUseCase = new GetProductUseCaseImpl(productRepository);
        return (id) -> {
            transactionTemplate.setReadOnly(true);
            return transactionTemplate.execute(status -> pureUseCase.execute(id));
        };
    }

    @Bean
    public GetProductEditViewUseCase getProductEditViewUseCase(
            ProductRepository productRepository,
            TransactionTemplate transactionTemplate) {
        GetProductEditViewUseCase pureUseCase = new GetProductEditViewUseCaseImpl(productRepository);
        return (id) -> {
            transactionTemplate.setReadOnly(true);
            return transactionTemplate.execute(status -> pureUseCase.execute(id));
        };
    }

    @Bean
    public GetProductLookupUseCase getProductLookupUseCase(
            ProductRepository productRepository,
            TransactionTemplate transactionTemplate) {
        GetProductLookupUseCase pureUseCase = new GetProductLookupUseCaseImpl(productRepository);
        return new GetProductLookupUseCase() {
            @Override
            public com.solusi.erp.core.dto.LookupDto getById(Long id) {
                transactionTemplate.setReadOnly(true);
                return transactionTemplate.execute(status -> pureUseCase.getById(id));
            }

            @Override
            public java.util.List<com.solusi.erp.core.dto.LookupDto> search(String keyword, int limit) {
                transactionTemplate.setReadOnly(true);
                return transactionTemplate.execute(status -> pureUseCase.search(keyword, limit));
            }
        };
    }
}
