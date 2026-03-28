package com.solusi.erp.master.partyroletype.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;
import com.solusi.erp.master.partyroletype.domain.repository.PartyRoleTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeletePartyRoleTypeUseCase Tests")
class DeletePartyRoleTypeUseCaseTest {

    @Mock
    private PartyRoleTypeRepository repository;

    private DeletePartyRoleTypeUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeletePartyRoleTypeUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute performs soft delete by setting isActive to false")
    void execute_performsSoftDelete() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        PartyRoleType existing = new PartyRoleType(metadata, "PRT-001", "Customer", null, true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(PartyRoleType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(1L);

        assertThat(existing.getIsActive()).isFalse();
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("execute throws DomainException when party role type is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L));
    }
}
