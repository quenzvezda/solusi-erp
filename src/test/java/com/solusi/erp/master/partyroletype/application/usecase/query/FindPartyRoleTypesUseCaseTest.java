package com.solusi.erp.master.partyroletype.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;
import com.solusi.erp.master.partyroletype.domain.repository.PartyRoleTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindPartyRoleTypesUseCase Tests")
class FindPartyRoleTypesUseCaseTest {

    @Mock
    private PartyRoleTypeRepository repository;

    private FindPartyRoleTypesUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindPartyRoleTypesUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute delegates keyword and pageable to repository and returns its result")
    void execute_delegatesKeywordAndPageableToRepository() {
        Pageable pageable = Pageable.of(0, 20);
        PartyRoleType prt = PartyRoleType.createNew("PRT-001", "Customer", null, true);
        Page<PartyRoleType> expectedPage = new Page<>(List.of(prt), 0, 20, 1L);

        when(repository.findAll("cust", pageable)).thenReturn(expectedPage);

        Page<PartyRoleType> result = useCase.execute("cust", pageable);

        assertThat(result).isEqualTo(expectedPage);
    }
}
