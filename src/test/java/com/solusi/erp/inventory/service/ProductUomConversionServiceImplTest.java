package com.solusi.erp.inventory.service;

import com.solusi.erp.inventory.dto.ProductUomConversionRequest;
import com.solusi.erp.inventory.dto.ProductUomConversionResponse;
import com.solusi.erp.inventory.mapper.ProductUomConversionMapper;
import com.solusi.erp.inventory.model.Product;
import com.solusi.erp.inventory.model.ProductUomConversion;
import com.solusi.erp.inventory.model.UnitOfMeasure;
import com.solusi.erp.inventory.repository.ProductRepository;
import com.solusi.erp.inventory.repository.ProductUomConversionRepository;
import com.solusi.erp.inventory.repository.UnitOfMeasureRepository;
import com.solusi.erp.inventory.service.impl.ProductUomConversionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductUomConversionServiceImplTest {

    @Mock
    private ProductUomConversionRepository repository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UnitOfMeasureRepository uomRepository;
    @Mock
    private ProductUomConversionMapper mapper;
    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ProductUomConversionServiceImpl service;

    private Product product;
    private UnitOfMeasure baseUom;
    private UnitOfMeasure boxUom;
    private ProductUomConversionRequest request;

    @BeforeEach
    void setUp() {
        baseUom = new UnitOfMeasure();
        baseUom.setId(1L);
        baseUom.setName("Pieces");

        boxUom = new UnitOfMeasure();
        boxUom.setId(2L);
        boxUom.setName("Box");

        product = new Product();
        product.setId(10L);
        product.setName("Test Product");
        product.setUom(baseUom);

        request = new ProductUomConversionRequest();
        request.setProductId(10L);
        request.setFromUomId(2L);
        request.setConversionFactor(new BigDecimal("10.00"));
    }

    @Test
    void create_ShouldSucceed_WhenValid() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(repository.existsByProductIdAndFromUomId(10L, 2L)).thenReturn(false);
        when(mapper.toEntity(any())).thenReturn(new ProductUomConversion());
        when(productRepository.getReferenceById(10L)).thenReturn(product);
        when(uomRepository.getReferenceById(2L)).thenReturn(boxUom);
        when(repository.save(any())).thenReturn(new ProductUomConversion());
        when(mapper.toResponse(any())).thenReturn(new ProductUomConversionResponse());

        assertNotNull(service.create(request));
        verify(repository).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenFactorIsZeroOrNegative() {
        request.setConversionFactor(BigDecimal.ZERO);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Factor must be positive");

        Exception exception = assertThrows(RuntimeException.class, () -> service.create(request));
        assertTrue(exception.getMessage().contains("Factor must be positive"));
    }

    @Test
    void create_ShouldThrowException_WhenFromUomIsSameAsBaseUom() {
        request.setFromUomId(1L); // Same as Pieces
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Self conversion not allowed");

        Exception exception = assertThrows(RuntimeException.class, () -> service.create(request));
        assertTrue(exception.getMessage().contains("Self conversion not allowed"));
    }

    @Test
    void create_ShouldThrowException_WhenDuplicateFound() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(repository.existsByProductIdAndFromUomId(10L, 2L)).thenReturn(true);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Duplicate conversion");

        Exception exception = assertThrows(RuntimeException.class, () -> service.create(request));
        assertTrue(exception.getMessage().contains("Duplicate conversion"));
    }

    @Test
    void create_ShouldRoundToTwoDecimalPlaces() {
        request.setConversionFactor(new BigDecimal("10.123456"));
        
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(repository.existsByProductIdAndFromUomId(10L, 2L)).thenReturn(false);
        // Use a real entity or a stub that records sets
        ProductUomConversion entity = new ProductUomConversion();
        when(mapper.toEntity(any())).thenReturn(entity);
        when(productRepository.getReferenceById(10L)).thenReturn(product);
        when(uomRepository.getReferenceById(2L)).thenReturn(boxUom);
        
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any())).thenReturn(new ProductUomConversionResponse());

        service.create(request);
        
        assertEquals(new BigDecimal("10.12"), entity.getConversionFactor());
    }

    @Test
    void delete_ShouldThrowException_WhenNotFound() {
        when(repository.existsById(1L)).thenReturn(false);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Not found");

        assertThrows(RuntimeException.class, () -> service.delete(1L));
    }
}
