package com.solusi.erp.inventory.product.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;
import java.math.BigDecimal;

/**
 * Implementation of UpdateProductUseCase.
 * Pure Java.
 */
public class UpdateProductUseCaseImpl implements UpdateProductUseCase {

    private final ProductRepository productRepository;

    public UpdateProductUseCaseImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product execute(
        Long id,
        String name,
        String barcode,
        String note,
        Long categoryId,
        Long uomId,
        Long brandId,
        String hscode,
        boolean isActive,
        boolean isSerialized,
        BigDecimal minStock,
        BigDecimal maxStock,
        BigDecimal weightNet,
        BigDecimal weightGross,
        Long weightUomId,
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        Long dimensionUomId
    ) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.product.not-found"));

        product.updateInfo(name, barcode, note, categoryId, uomId, brandId, hscode);
        product.updateStatus(isActive);
        
        // Manual updates for other fields since they are simple setters/logic for now
        // In more complex DDD, these might be grouped into Value Objects.
        // For this refactor, we maintain simplicity.
        
        // We'll add methods to domain model if needed, but for now we'll do it via a more comprehensive updateInfo
        // or just re-create the Product object if it's not truly an aggregate root with complex state transitions.
        // But the current Product domain model uses private fields without setters to enforce purity.
        
        // Let's update the Domain Model to have a more complete update method.
        return productRepository.save(new Product(
            product.getMetadata(),
            product.getCode(),
            name,
            barcode,
            note,
            categoryId,
            uomId,
            brandId,
            hscode,
            isActive,
            isSerialized,
            minStock,
            maxStock,
            weightNet,
            weightGross,
            weightUomId,
            length,
            width,
            height,
            dimensionUomId
        ));
    }
}
