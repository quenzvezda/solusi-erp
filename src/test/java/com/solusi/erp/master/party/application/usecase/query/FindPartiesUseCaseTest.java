package com.solusi.erp.master.party.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindPartiesUseCase Tests")
class FindPartiesUseCaseTest {

    @Mock
    private PartyRepository repository;

    private FindPartiesUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindPartiesUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute delegates to repository and returns page result")
    void execute_delegatesToRepository() {
        Pageable pageable = Pageable.of(0, 20);
        Party party = Party.createNew(
                "PTY-001", "Acme Corp", "Mr.", PartyType.ORGANIZATION,
                null, true, null, null,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>()
        );
        Page<Party> expected = new Page<>(List.of(party), 0, 20, 1L);
        when(repository.findAll("acme", pageable)).thenReturn(expected);

        Page<Party> result = useCase.execute("acme", pageable);

        assertThat(result).isEqualTo(expected);
    }
}
