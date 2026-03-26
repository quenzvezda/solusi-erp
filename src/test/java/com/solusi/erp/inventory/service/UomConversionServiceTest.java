package com.solusi.erp.inventory.service;

import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import com.solusi.erp.inventory.model.ProductUomConversion;
import com.solusi.erp.inventory.model.UnitOfMeasure;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.repository.ProductUomConversionRepository;
import com.solusi.erp.inventory.service.impl.UomConversionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UomConversionServiceTest {

    @Mock
    private JpaProductRepository productRepository;

    @Mock
    private ProductUomConversionRepository conversionRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private UomConversionServiceImpl uomConversionService;

    private ProductEntity product;
    private UnitOfMeasure baseUom;
    private UnitOfMeasure sourceUom;

    @BeforeEach
    void setUp() {
        baseUom = new UnitOfMeasure();
        baseUom.setId(10L);
        baseUom.setName("Pieces");

        sourceUom = new UnitOfMeasure();
        sourceUom.setId(20L);
        sourceUom.setName("Box");

        product = new ProductEntity();
        product.setId(1L);
        product.setCode("P001");
        product.setUom(baseUom);
    }

    @Test
    void shouldReturnSameQuantityIfSourceIsBaseUom() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        BigDecimal result = uomConversionService.convertToBaseUom(100L, 1L, new BigDecimal("10"));

        assertEquals(0, result.compareTo(new BigDecimal("10")));
    }

    @Test
    void shouldConvertUsingFactor() {
        ProductUomConversion conv = new ProductUomConversion();
        conv.setConversionFactor(new BigDecimal("24"));

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(conversionRepository.findByProductIdAndFromUomIdAndToUomId(100L, 2L, 1L))
                .thenReturn(Optional.of(conv));

        BigDecimal result = uomConversionService.convertToBaseUom(100L, 2L, new BigDecimal("2"));

        assertEquals(0, result.compareTo(new BigDecimal("48")));
    }

    @Test
    void shouldThrowExceptionIfConversionNotFound() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(conversionRepository.findByProductIdAndFromUomIdAndToUomId(100L, 2L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
                uomConversionService.convertToBaseUom(100L, 2L, new BigDecimal("2")));
    }
}
