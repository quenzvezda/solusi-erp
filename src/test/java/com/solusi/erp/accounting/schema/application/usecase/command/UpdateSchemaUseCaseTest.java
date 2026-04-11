package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSchemaUseCase Tests")
class UpdateSchemaUseCaseTest {

    @Mock private SchemaRepository repository;
    private UpdateSchemaUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateSchemaUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute updates and returns schema")
    void execute_updatesAndReturnsSchema() {
        AuditMetadata meta = new AuditMetadata(1L, 1L, null, null, null, null);
        AccountingSchema existing = new AccountingSchema(meta, SchemaEventType.GOODS_RECEIPT,
                "old", 1L, 2L, true);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByEventTypeAndIsActiveTrue(SchemaEventType.GOODS_RECEIPT))
                .thenReturn(Optional.of(existing)); // same schema, no conflict
        when(repository.save(any(AccountingSchema.class))).thenAnswer(i -> i.getArgument(0));

        AccountingSchema result = useCase.execute(1L, "new desc", 10L, 20L, true);

        assertThat(result.getDescription()).isEqualTo("new desc");
        assertThat(result.getDebitAccountId()).isEqualTo(10L);
        verify(repository).save(any(AccountingSchema.class));
    }

    @Test
    @DisplayName("execute throws when schema not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L, "desc", 1L, 2L, true))
                .isInstanceOf(DomainException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("execute throws when another active schema has same event type")
    void execute_throwsWhenDuplicateActiveEventType() {
        AuditMetadata meta1 = new AuditMetadata(1L, 1L, null, null, null, null);
        AccountingSchema existing = new AccountingSchema(meta1, SchemaEventType.GOODS_RECEIPT,
                "old", 1L, 2L, true);
        AuditMetadata meta2 = new AuditMetadata(2L, 1L, null, null, null, null);
        AccountingSchema otherActive = new AccountingSchema(meta2, SchemaEventType.GOODS_RECEIPT,
                "other", 3L, 4L, true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByEventTypeAndIsActiveTrue(SchemaEventType.GOODS_RECEIPT))
                .thenReturn(Optional.of(otherActive)); // different ID = conflict

        assertThatThrownBy(() -> useCase.execute(1L, "desc", 10L, 20L, true))
                .isInstanceOf(DomainException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("execute skips duplicate check when isActive is false")
    void execute_skipsDuplicateCheckWhenInactive() {
        AuditMetadata meta = new AuditMetadata(1L, 1L, null, null, null, null);
        AccountingSchema existing = new AccountingSchema(meta, SchemaEventType.GOODS_RECEIPT,
                "old", 1L, 2L, true);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(AccountingSchema.class))).thenAnswer(i -> i.getArgument(0));

        AccountingSchema result = useCase.execute(1L, "desc", 10L, 20L, false);

        assertThat(result.getIsActive()).isFalse();
        verify(repository, never()).findByEventTypeAndIsActiveTrue(any());
    }
}
