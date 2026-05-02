package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.domain.port.SchemaInUseChecker;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.DeleteResult;
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
@DisplayName("DeleteSchemaUseCase Tests")
class DeleteSchemaUseCaseTest {

    @Mock private SchemaRepository repository;
    @Mock private SchemaInUseChecker inUseChecker;
    private DeleteSchemaUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteSchemaUseCaseImpl(repository, inUseChecker);
    }

    @Test
    @DisplayName("execute hard-deletes when not in use")
    void execute_hardDeletesWhenNotInUse() {
        AuditMetadata meta = new AuditMetadata(1L, 1L, null, null, null, null);
        AccountingSchema schema = new AccountingSchema(meta, SchemaEventType.GOODS_RECEIPT,
                "desc", 1L, 2L, null, true);
        when(repository.findById(1L)).thenReturn(Optional.of(schema));
        when(inUseChecker.isInUse(1L)).thenReturn(false);

        DeleteResult result = useCase.execute(1L);

        assertThat(result).isEqualTo(DeleteResult.HARD_DELETED);
        verify(repository).delete(1L);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("execute soft-deletes when in use")
    void execute_softDeletesWhenInUse() {
        AuditMetadata meta = new AuditMetadata(1L, 1L, null, null, null, null);
        AccountingSchema schema = new AccountingSchema(meta, SchemaEventType.GOODS_RECEIPT,
                "desc", 1L, 2L, null, true);
        when(repository.findById(1L)).thenReturn(Optional.of(schema));
        when(inUseChecker.isInUse(1L)).thenReturn(true);

        DeleteResult result = useCase.execute(1L);

        assertThat(result).isEqualTo(DeleteResult.SOFT_DELETED);
        verify(repository).save(any(AccountingSchema.class));
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("execute throws when schema not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
                .isInstanceOf(DomainException.class);
    }
}
