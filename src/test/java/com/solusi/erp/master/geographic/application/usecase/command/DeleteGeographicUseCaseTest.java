package com.solusi.erp.master.geographic.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.domain.port.GeographicInUseChecker;
import com.solusi.erp.master.geographic.domain.repository.GeographicRepository;
import com.solusi.erp.master.shared.model.GeographicType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteGeographicUseCase Tests")
class DeleteGeographicUseCaseTest {

    @Mock
    private GeographicRepository repository;

    @Mock
    private GeographicInUseChecker inUseChecker;

    private DeleteGeographicUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteGeographicUseCaseImpl(repository, inUseChecker);
    }

    @Test
    @DisplayName("execute hard-deletes when geographic is not in use")
    void execute_hardDeletesWhenNotInUse() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Geographic existing = new Geographic(metadata, "ID", "Indonesia",
                GeographicType.COUNTRY, null, null, Boolean.TRUE);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(inUseChecker.isInUse(1L)).thenReturn(false);

        DeleteResult result = useCase.execute(1L);

        verify(repository).delete(1L);
        assertEquals(DeleteResult.HARD_DELETED, result);
    }

    @Test
    @DisplayName("execute soft-deletes when geographic is in use")
    void execute_softDeletesWhenInUse() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Geographic existing = new Geographic(metadata, "ID", "Indonesia",
                GeographicType.COUNTRY, null, null, Boolean.TRUE);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(inUseChecker.isInUse(1L)).thenReturn(true);

        DeleteResult result = useCase.execute(1L);

        verify(repository).save(existing);
        assertEquals(DeleteResult.SOFT_DELETED, result);
    }

    @Test
    @DisplayName("execute throws DomainException when geographic is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L));
    }
}

