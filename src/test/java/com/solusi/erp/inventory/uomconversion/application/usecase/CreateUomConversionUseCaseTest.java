package com.solusi.erp.inventory.uomconversion.application.usecase;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.model.UnitOfMeasure;
import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import com.solusi.erp.inventory.repository.UnitOfMeasureRepository;
import com.solusi.erp.inventory.uomconversion.application.usecase.command.CreateUomConversionUseCaseImpl;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;
import com.solusi.erp.inventory.uomconversion.domain.repository.UomConversionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUomConversionUseCaseTest {

    @Mock UomConversionRepository repository;
    @Mock JpaProductRepository productRepo;
    @Mock UnitOfMeasureRepository uomRepo;

    CreateUomConversionUseCaseImpl useCase;

    private ProductEntity mockProduct;
    private UnitOfMeasure baseUom;
    private UnitOfMeasure fromUom;

    @BeforeEach
    void setUp() {
        useCase = new CreateUomConversionUseCaseImpl(repository, productRepo, uomRepo);

        baseUom = new UnitOfMeasure("PCS", "Pieces", UomType.UNIT);
        setField(baseUom, "id", 10L);

        fromUom = new UnitOfMeasure("BOX", "Box", UomType.UNIT);
        setField(fromUom, "id", 20L);

        mockProduct = new ProductEntity();
        setField(mockProduct, "id", 1L);
        mockProduct.setCode("PRD-001");
        mockProduct.setName("Product 1");
        mockProduct.setUom(baseUom);
    }

    @Test
    void execute_success() {
        when(productRepo.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(repository.existsByProductIdAndFromUomId(1L, 20L)).thenReturn(false);
        when(uomRepo.findById(20L)).thenReturn(Optional.of(fromUom));

        UomConversion saved = new UomConversion(
            AuditMetadata.empty(), 1L, "PRD-001", "Product 1",
            20L, "Box", 10L, "Pieces", new BigDecimal("12.00"));
        when(repository.save(any())).thenReturn(saved);

        UomConversion result = useCase.execute(1L, 20L, new BigDecimal("12.00"));

        assertThat(result).isNotNull();
        assertThat(result.getFromUomName()).isEqualTo("Box");
        verify(repository).save(any());
    }

    @Test
    void execute_selfConversion_shouldThrow() {
        when(productRepo.findById(1L)).thenReturn(Optional.of(mockProduct));

        assertThatThrownBy(() -> useCase.execute(1L, 10L, new BigDecimal("1.00")))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("self-conversion");
    }

    @Test
    void execute_duplicate_shouldThrow() {
        when(productRepo.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(repository.existsByProductIdAndFromUomId(1L, 20L)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(1L, 20L, new BigDecimal("12.00")))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("duplicate");
    }

    @Test
    void execute_invalidFactor_shouldThrow() {
        when(productRepo.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(repository.existsByProductIdAndFromUomId(1L, 20L)).thenReturn(false);
        when(uomRepo.findById(20L)).thenReturn(Optional.of(fromUom));

        assertThatThrownBy(() -> useCase.execute(1L, 20L, BigDecimal.ZERO))
            .isInstanceOf(DomainException.class);
    }

    @Test
    void execute_productNotFound_shouldThrow() {
        when(productRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, 20L, new BigDecimal("12.00")))
            .isInstanceOf(DomainException.class);
    }

    // Reflection helper for setting JPA entity fields without setters
    private void setField(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = findField(obj.getClass(), fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) return findField(clazz.getSuperclass(), name);
            throw e;
        }
    }
}
