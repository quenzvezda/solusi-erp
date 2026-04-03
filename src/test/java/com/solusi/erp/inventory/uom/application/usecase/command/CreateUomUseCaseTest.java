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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUomUseCase Tests")
class CreateUomUseCaseTest {

    @Mock
    private UomRepository repository;

    private CreateUomUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateUomUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute throws DomainException when code already exists")
    void execute_throwsWhenDuplicateCode() {
        when(repository.existsByCode("KG")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("KG", "Kilogram", UomType.WEIGHT))
            .isInstanceOf(DomainException.class)
            .hasMessage("msg.error.uom.duplicate-code");
    }

    @Test
    @DisplayName("execute creates and saves UnitOfMeasure when code is unique")
    void execute_createsAndSavesUom() {
        when(repository.existsByCode("KG")).thenReturn(false);
        when(repository.save(any(UnitOfMeasure.class))).thenAnswer(inv -> inv.getArgument(0));

        UnitOfMeasure result = useCase.execute("KG", "Kilogram", UomType.WEIGHT);

        assertThat(result.getCode()).isEqualTo("KG");
        assertThat(result.getName()).isEqualTo("Kilogram");
        assertThat(result.getType()).isEqualTo(UomType.WEIGHT);
    }

    @Test
    @DisplayName("execute returns result from repository (with persisted id)")
    void execute_returnsResultFromRepository() {
        AuditMetadata metadata = new AuditMetadata(10L, 1L, null, null, null, null);
        UnitOfMeasure persisted = new UnitOfMeasure(metadata, "KG", "Kilogram", UomType.WEIGHT);

        when(repository.existsByCode("KG")).thenReturn(false);
        when(repository.save(any(UnitOfMeasure.class))).thenReturn(persisted);

        UnitOfMeasure result = useCase.execute("KG", "Kilogram", UomType.WEIGHT);

        assertThat(result.getId()).isEqualTo(10L);
    }
}
