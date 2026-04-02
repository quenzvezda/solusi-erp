package com.solusi.erp.master.partyroletype.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;
import com.solusi.erp.master.partyroletype.domain.port.PartyRoleTypeInUseChecker;
import com.solusi.erp.master.partyroletype.domain.repository.PartyRoleTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeletePartyRoleTypeUseCase Tests")
class DeletePartyRoleTypeUseCaseTest {

    @Mock
    private PartyRoleTypeRepository repository;

    @Mock
    private PartyRoleTypeInUseChecker inUseChecker;

    private DeletePartyRoleTypeUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeletePartyRoleTypeUseCaseImpl(repository, inUseChecker);
    }

    @Test
    @DisplayName("execute hard-deletes when party role type is not in use")
    void execute_hardDeletesWhenNotInUse() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        PartyRoleType existing = new PartyRoleType(metadata, "PRT-001", "Customer", null, true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(inUseChecker.isInUse(1L)).thenReturn(false);

        DeleteResult result = useCase.execute(1L);

        verify(repository).delete(1L);
        assertEquals(DeleteResult.HARD_DELETED, result);
    }

    @Test
    @DisplayName("execute soft-deletes when party role type is in use")
    void execute_softDeletesWhenInUse() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        PartyRoleType existing = new PartyRoleType(metadata, "PRT-001", "Customer", null, true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(inUseChecker.isInUse(1L)).thenReturn(true);
        when(repository.save(any(PartyRoleType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeleteResult result = useCase.execute(1L);

        assertThat(existing.getIsActive()).isFalse();
        verify(repository).save(existing);
        assertEquals(DeleteResult.SOFT_DELETED, result);
    }

    @Test
    @DisplayName("execute throws DomainException when party role type is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L));
    }
}
