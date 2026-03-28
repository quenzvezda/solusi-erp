package com.solusi.erp.master.party.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.service.SequenceGeneratorService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePartyUseCase Tests")
class CreatePartyUseCaseTest {

    @Mock
    private PartyRepository repository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    private CreatePartyUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreatePartyUseCaseImpl(repository, sequenceGeneratorService);
    }

    @Test
    @DisplayName("execute generates code from sequence 'PARTY' and saves party")
    void execute_generatesCodeAndSavesParty() {
        when(sequenceGeneratorService.generate("PARTY")).thenReturn("PTY-001");
        when(repository.save(any(Party.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Party result = useCase.execute("Acme Corp", "Mr.", PartyType.ORGANIZATION,
                "Notes", true, "email@test.com", "0812", new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new HashSet<>());

        assertThat(result.getCode()).isEqualTo("PTY-001");
        assertThat(result.getName()).isEqualTo("Acme Corp");
    }

    @Test
    @DisplayName("execute returns result from repository (with persisted id)")
    void execute_returnsResultFromRepository() {
        AuditMetadata metadata = new AuditMetadata(10L, 1L, null, null, null, null);
        Party persisted = new Party(metadata, "PTY-001", "Mr.", "Acme Corp", PartyType.ORGANIZATION,
                "Notes", true, "email@test.com", "0812", new HashSet<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        when(sequenceGeneratorService.generate("PARTY")).thenReturn("PTY-001");
        when(repository.save(any(Party.class))).thenReturn(persisted);

        Party result = useCase.execute("Acme Corp", "Mr.", PartyType.ORGANIZATION,
                "Notes", true, "email@test.com", "0812", new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new HashSet<>());

        assertThat(result.getId()).isEqualTo(10L);
    }
}
