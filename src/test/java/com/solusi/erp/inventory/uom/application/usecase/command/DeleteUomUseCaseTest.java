package com.solusi.erp.inventory.uom.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.uom.domain.model.UomType;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;
import com.solusi.erp.inventory.uom.domain.repository.UomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteUomUseCase Tests")
class DeleteUomUseCaseTest {

    @Mock
    private UomRepository repository;

    private DeleteUomUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteUomUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute throws DomainException when UOM not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L))
            .isInstanceOf(DomainException.class)
            .hasMessage("msg.error.uom.notfound");
    }

    @Test
    @DisplayName("execute deletes UOM when found")
    void execute_deletesWhenFound() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        UnitOfMeasure existing = new UnitOfMeasure(metadata, "KG", "Kilogram", UomType.WEIGHT);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        useCase.execute(1L);

        verify(repository, times(1)).delete(1L);
    }
}
