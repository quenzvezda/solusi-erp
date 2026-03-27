package com.solusi.erp.inventory.uomconversion.application.usecase;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.model.UnitOfMeasure;
import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.repository.UnitOfMeasureRepository;
import com.solusi.erp.inventory.uomconversion.application.usecase.command.UpdateUomConversionUseCaseImpl;
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
class UpdateUomConversionUseCaseTest {

    @Mock UomConversionRepository repository;
    @Mock UnitOfMeasureRepository uomRepo;

    UpdateUomConversionUseCaseImpl useCase;

    private UomConversion existing;
    private UnitOfMeasure fromUom;

    @BeforeEach
    void setUp() {
        useCase = new UpdateUomConversionUseCaseImpl(repository, uomRepo);

        existing = new UomConversion(
            new AuditMetadata(1L, 1L, null, null, null, null),
            10L, "PRD-001", "Product 1",
            20L, "Box", 30L, "Pieces", new BigDecimal("12.00"));

        fromUom = new UnitOfMeasure("CART", "Carton", UomType.UNIT);
        setField(fromUom, "id", 40L);
    }

    @Test
    void execute_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByProductIdAndFromUomIdAndIdNot(10L, 40L, 1L)).thenReturn(false);
        when(uomRepo.findById(40L)).thenReturn(Optional.of(fromUom));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UomConversion result = useCase.execute(1L, 40L, new BigDecimal("24.00"));

        assertThat(result.getFromUomId()).isEqualTo(40L);
        assertThat(result.getFromUomName()).isEqualTo("Carton");
        assertThat(result.getConversionFactor()).isEqualByComparingTo(new BigDecimal("24.00"));
    }

    @Test
    void execute_notFound_shouldThrow() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, 40L, new BigDecimal("12.00")))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("not-found");
    }

    @Test
    void execute_duplicate_shouldThrow() {
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByProductIdAndFromUomIdAndIdNot(10L, 40L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(1L, 40L, new BigDecimal("24.00")))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("duplicate");
    }

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
