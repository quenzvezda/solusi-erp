package com.solusi.erp.inventory.uom.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;
import com.solusi.erp.inventory.uom.domain.repository.UomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindUomsUseCase Tests")
class FindUomsUseCaseTest {

    @Mock
    private UomRepository repository;

    private FindUomsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindUomsUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute forwards keyword and pageable to repository")
    void execute_forwardsPaginationToRepository() {
        Pageable pageable = Pageable.of(0, 10);
        UnitOfMeasure uom = UnitOfMeasure.createNew("KG", "Kilogram", UomType.WEIGHT);
        Page<UnitOfMeasure> expected = new Page<>(List.of(uom), 0, 10, 1L);

        when(repository.findAll("kg", pageable)).thenReturn(expected);

        Page<UnitOfMeasure> result = useCase.execute("kg", pageable);

        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getCode()).isEqualTo("KG");
    }
}
