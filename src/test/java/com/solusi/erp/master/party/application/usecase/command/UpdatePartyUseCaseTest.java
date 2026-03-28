package com.solusi.erp.master.party.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.model.PartyType;
import com.solusi.erp.master.party.domain.model.Party;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdatePartyUseCase Tests")
class UpdatePartyUseCaseTest {

    @Mock
    private PartyRepository repository;

    private UpdatePartyUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdatePartyUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute updates party fields and preserves code")
    void execute_updatesPartyAndPreservesCode() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Party existing = new Party(metadata, "PTY-001", "Mr.", "Old Name", PartyType.PERSON,
                "Old Notes", true, "old@test.com", "0811", new HashSet<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Party.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Party result = useCase.execute(1L, "New Name", "Dr.", PartyType.ORGANIZATION,
                "New Notes", false, "new@test.com", "0822", new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new HashSet<>());

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getSalutation()).isEqualTo("Dr.");
        assertThat(result.getCode()).isEqualTo("PTY-001");
        assertThat(result.getType()).isEqualTo(PartyType.ORGANIZATION);
    }

    @Test
    @DisplayName("execute throws DomainException when party is not found")
    void execute_throwsDomainException_whenPartyNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L, "Name", null, PartyType.PERSON,
                null, true, null, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>()));
    }
}
