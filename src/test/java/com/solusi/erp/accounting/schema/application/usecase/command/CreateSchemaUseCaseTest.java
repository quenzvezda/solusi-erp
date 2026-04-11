package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateSchemaUseCase Tests")
class CreateSchemaUseCaseTest {

    @Mock private SchemaRepository repository;
    private CreateSchemaUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateSchemaUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute saves and returns schema")
    void execute_savesAndReturnsSchema() {
        when(repository.existsByEventTypeAndIsActiveTrue(SchemaEventType.GOODS_RECEIPT)).thenReturn(false);
        when(repository.save(any(AccountingSchema.class))).thenAnswer(i -> i.getArgument(0));

        AccountingSchema result = useCase.execute(SchemaEventType.GOODS_RECEIPT, "GR schema",
                1L, 2L, true);

        assertThat(result.getEventType()).isEqualTo(SchemaEventType.GOODS_RECEIPT);
        assertThat(result.getDescription()).isEqualTo("GR schema");
        verify(repository).save(any(AccountingSchema.class));
    }

    @Test
    @DisplayName("execute throws when active duplicate event type exists")
    void execute_throwsWhenDuplicateActiveEvent() {
        when(repository.existsByEventTypeAndIsActiveTrue(SchemaEventType.GOODS_RECEIPT)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(SchemaEventType.GOODS_RECEIPT, "desc",
                1L, 2L, true))
                .isInstanceOf(DomainException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("execute allows inactive duplicate — no duplicate check for inactive")
    void execute_allowsInactiveDuplicate() {
        // isActive=false bypasses the duplicate check
        when(repository.save(any(AccountingSchema.class))).thenAnswer(i -> i.getArgument(0));

        AccountingSchema result = useCase.execute(SchemaEventType.GOODS_RECEIPT, "desc",
                1L, 2L, false);

        assertThat(result).isNotNull();
        verify(repository).save(any(AccountingSchema.class));
        // existsByEventTypeAndIsActiveTrue should NOT be called when isActive is false
        verify(repository, never()).existsByEventTypeAndIsActiveTrue(any());
    }
}
