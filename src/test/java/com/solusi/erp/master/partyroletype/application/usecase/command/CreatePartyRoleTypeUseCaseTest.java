package com.solusi.erp.master.partyroletype.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;
import com.solusi.erp.master.partyroletype.domain.repository.PartyRoleTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePartyRoleTypeUseCase Tests")
class CreatePartyRoleTypeUseCaseTest {

    @Mock
    private PartyRoleTypeRepository repository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    private CreatePartyRoleTypeUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreatePartyRoleTypeUseCaseImpl(repository, sequenceGeneratorService);
    }

    @Test
    @DisplayName("execute generates code from sequence and saves party role type")
    void execute_generatesCodeAndSavesPartyRoleType() {
        when(sequenceGeneratorService.generate("PARTY-ROLE-TYPE")).thenReturn("PRT-001");
        when(repository.save(any(PartyRoleType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PartyRoleType result = useCase.execute("Customer", "A customer role", true);

        assertThat(result.getCode()).isEqualTo("PRT-001");
        assertThat(result.getName()).isEqualTo("Customer");
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("execute returns result from repository with persisted id")
    void execute_returnsResultFromRepository() {
        AuditMetadata metadata = new AuditMetadata(10L, 1L, null, null, null, null);
        PartyRoleType persisted = new PartyRoleType(metadata, "PRT-001", "Customer", null, true);

        when(sequenceGeneratorService.generate("PARTY-ROLE-TYPE")).thenReturn("PRT-001");
        when(repository.save(any(PartyRoleType.class))).thenReturn(persisted);

        PartyRoleType result = useCase.execute("Customer", null, true);

        assertThat(result.getId()).isEqualTo(10L);
    }
}
