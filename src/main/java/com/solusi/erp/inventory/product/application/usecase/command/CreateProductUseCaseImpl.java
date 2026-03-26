package com.solusi.erp.inventory.product.application.usecase.command;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;
import java.math.BigDecimal;

/**
 * Implementation of CreateProductUseCase.
 * Pure Java (Port interface for SequenceGenerator is implied via constructor).
 */
public class CreateProductUseCaseImpl implements CreateProductUseCase {

    private final ProductRepository productRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreateProductUseCaseImpl(ProductRepository productRepository, SequenceGeneratorService sequenceGeneratorService) {
        this.productRepository = productRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public Product execute(
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
        // Auto-generate code
        String code = sequenceGeneratorService.generate("PRODUCT");

        Product product = Product.createNew(
            code, name, barcode, note, categoryId, uomId, brandId, hscode,
            isActive, isSerialized, minStock, maxStock,
            weightNet, weightGross, weightUomId,
            length, width, height, dimensionUomId
        );

        return productRepository.save(product);
    }
}
