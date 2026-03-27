package com.solusi.erp.inventory.uom.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;
import com.solusi.erp.inventory.uom.domain.repository.UomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUomUseCase Tests")
class UpdateUomUseCaseTest {

    @Mock
    private UomRepository repository;

    private UpdateUomUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateUomUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute throws DomainException when UOM not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, "New Name", UomType.VOLUME))
            .isInstanceOf(DomainException.class)
            .hasMessage("msg.error.uom.notfound");
    }

    @Test
    @DisplayName("execute updates name and type then saves")
    void execute_updatesAndSaves() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        UnitOfMeasure existing = new UnitOfMeasure(metadata, "KG", "Kilogram", UomType.WEIGHT);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(UnitOfMeasure.class))).thenAnswer(inv -> inv.getArgument(0));

        UnitOfMeasure result = useCase.execute(1L, "Liter", UomType.VOLUME);

        assertThat(result.getName()).isEqualTo("Liter");
        assertThat(result.getType()).isEqualTo(UomType.VOLUME);
        assertThat(result.getCode()).isEqualTo("KG");
    }
}
