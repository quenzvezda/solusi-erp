package com.solusi.erp.accounting.schema.application.usecase.query;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindSchemasUseCase Tests")
class FindSchemasUseCaseTest {

    @Mock private SchemaRepository repository;
    private FindSchemasUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindSchemasUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute delegates to repository")
    void execute_delegatesToRepository() {
        Pageable pageable = new Pageable(0, 20);
        Page<AccountingSchema> expected = new Page<>(List.of(), 0, 20, 0L);
        when(repository.findAll("goods", pageable)).thenReturn(expected);

        Page<AccountingSchema> result = useCase.execute("goods", pageable);

        assertThat(result).isSameAs(expected);
        verify(repository).findAll("goods", pageable);
    }
}
