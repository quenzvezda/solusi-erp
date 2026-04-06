package com.solusi.erp.master.party.application.usecase.query;

import com.solusi.erp.master.party.domain.model.Party;
import com.solusi.erp.master.party.domain.repository.PartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindPartiesForLookupUseCaseImplTest {

    @Mock
    private PartyRepository repository;

    private FindPartiesForLookupUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindPartiesForLookupUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute delegates to repository.findForLookup")
    void execute_delegatesToRepository() {
        List<Party> expected = List.of();
        when(repository.findForLookup("test")).thenReturn(expected);

        List<Party> result = useCase.execute("test");

        assertThat(result).isSameAs(expected);
        verify(repository).findForLookup("test");
    }

    @Test
    @DisplayName("executeByRoleType delegates to repository.findForLookupByRoleType")
    void executeByRoleType_delegatesToRepository() {
        List<Party> expected = List.of();
        when(repository.findForLookupByRoleType("keyword", "CUSTOMER", 5L)).thenReturn(expected);

        List<Party> result = useCase.executeByRoleType("keyword", "CUSTOMER", 5L);

        assertThat(result).isSameAs(expected);
        verify(repository).findForLookupByRoleType("keyword", "CUSTOMER", 5L);
    }
}
