package com.solusi.erp.master.party.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.shared.model.PartyType;
import com.solusi.erp.master.party.domain.model.Party;
import com.solusi.erp.master.party.domain.port.PartyInUseChecker;
import com.solusi.erp.master.party.domain.repository.PartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeletePartyUseCase Tests")
class DeletePartyUseCaseTest {

    @Mock
    private PartyRepository repository;

    @Mock
    private PartyInUseChecker inUseChecker;

    private DeletePartyUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeletePartyUseCaseImpl(repository, inUseChecker);
    }

    @Test
    @DisplayName("execute hard-deletes when party is not in use")
    void execute_hardDeletesWhenNotInUse() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Party existing = new Party(metadata, "PTY-001", "Mr.", "Acme Corp", PartyType.ORGANIZATION,
                null, true, null, null, new HashSet<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(inUseChecker.isInUse(1L)).thenReturn(false);

        DeleteResult result = useCase.execute(1L);

        verify(repository).delete(1L);
        assertEquals(DeleteResult.HARD_DELETED, result);
    }

    @Test
    @DisplayName("execute soft-deletes when party is in use")
    void execute_softDeletesWhenInUse() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Party existing = new Party(metadata, "PTY-001", "Mr.", "Acme Corp", PartyType.ORGANIZATION,
                null, true, null, null, new HashSet<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(inUseChecker.isInUse(1L)).thenReturn(true);

        DeleteResult result = useCase.execute(1L);

        verify(repository).softDelete(1L);
        assertEquals(DeleteResult.SOFT_DELETED, result);
    }

    @Test
    @DisplayName("execute throws DomainException when party is not found")
    void execute_throwsDomainException_whenPartyNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L));
    }
}

