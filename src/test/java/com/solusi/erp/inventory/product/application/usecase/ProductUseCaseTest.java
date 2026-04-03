package com.solusi.erp.inventory.product.application.usecase;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.product.application.usecase.command.*;
import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductUseCaseTest {

    private ProductRepository productRepository;
    private SequenceGeneratorService sequenceGeneratorService;
    private CreateProductUseCase createUseCase;
    private UpdateProductUseCase updateUseCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        sequenceGeneratorService = mock(SequenceGeneratorService.class);
        createUseCase = new CreateProductUseCaseImpl(productRepository, sequenceGeneratorService);
        updateUseCase = new UpdateProductUseCaseImpl(productRepository);
    }

    @Test
    void createProduct_ShouldSaveNewProduct() {
        when(sequenceGeneratorService.generate("PRODUCT")).thenReturn("PROD-001");
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = createUseCase.execute(
            "New Product", null, null, 1L, 1L, null, null,
            true, false, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, null,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null
        );

        assertNotNull(result);
        assertEquals("PROD-001", result.getCode());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_ShouldUpdateExisting() {
        Product existing = Product.createNew("OLD-CODE", "Old Name", null, null, 1L, 1L, null, null, true, false, null, null, null, null, null, null, null, null, null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = updateUseCase.execute(
            1L, "New Name", "BARCODE", "Note", 2L, 2L, null, "HS",
            true, false, BigDecimal.ONE, BigDecimal.TEN,
            BigDecimal.ZERO, BigDecimal.ZERO, null,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null
        );

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("BARCODE", result.getBarcode());
        verify(productRepository, times(1)).save(any(Product.class));
    }
}
