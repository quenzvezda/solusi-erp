package com.solusi.erp.inventory.uomconversion.infrastructure.service;

import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import com.solusi.erp.inventory.uomconversion.infrastructure.persistence.UomConversionEntity;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.uomconversion.infrastructure.persistence.UomConversionJpaRepository;
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
    private UomConversionJpaRepository conversionRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private UomConversionServiceImpl uomConversionService;

    private ProductEntity product;

    @BeforeEach
    void setUp() {
        product = new ProductEntity();
        product.setId(1L);
        product.setCode("P001");
        product.setUomId(10L);
    }

    @Test
    void shouldReturnSameQuantityIfSourceIsBaseUom() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        BigDecimal result = uomConversionService.convertToBaseUom(100L, 10L, new BigDecimal("10"));

        assertEquals(0, result.compareTo(new BigDecimal("10")));
    }

    @Test
    void shouldConvertUsingFactor() {
        UomConversionEntity conv = new UomConversionEntity();
        conv.setConversionFactor(new BigDecimal("24"));

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(conversionRepository.findByProductIdAndFromUomIdAndToUomId(100L, 2L, 10L))
                .thenReturn(Optional.of(conv));

        BigDecimal result = uomConversionService.convertToBaseUom(100L, 2L, new BigDecimal("2"));

        assertEquals(0, result.compareTo(new BigDecimal("48")));
    }

    @Test
    void shouldThrowExceptionIfConversionNotFound() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(conversionRepository.findByProductIdAndFromUomIdAndToUomId(100L, 2L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
                uomConversionService.convertToBaseUom(100L, 2L, new BigDecimal("2")));
    }
}
