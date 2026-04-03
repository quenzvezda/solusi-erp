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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdatePartyRoleTypeUseCase Tests")
class UpdatePartyRoleTypeUseCaseTest {

    @Mock
    private PartyRoleTypeRepository repository;

    private UpdatePartyRoleTypeUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdatePartyRoleTypeUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute updates mutable fields; code remains unchanged")
    void execute_updatesMutableFields() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        PartyRoleType existing = new PartyRoleType(metadata, "PRT-001", "Old Name", "Old Note", true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(PartyRoleType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PartyRoleType result = useCase.execute(1L, "New Name", "New Note", false);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getNote()).isEqualTo("New Note");
        assertThat(result.getCode()).isEqualTo("PRT-001");
    }

    @Test
    @DisplayName("execute throws DomainException when party role type is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L, "Name", null, true));
    }
}
