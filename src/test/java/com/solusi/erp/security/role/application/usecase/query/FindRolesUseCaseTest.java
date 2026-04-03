package com.solusi.erp.security.role.application.usecase.query;

import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.role.domain.repository.RoleRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FindRolesUseCaseTest {

    private final RoleRepository repository = mock(RoleRepository.class);
    private final FindRolesUseCase useCase = new FindRolesUseCaseImpl(repository);

    @Test
    void execute_returnsRepositoryResult() {
        List<Role> expected = List.of(Role.createNew("ROLE_USER", "desc", Set.of(1L)));
        when(repository.findAll()).thenReturn(expected);

        List<Role> actual = useCase.execute();

        assertThat(actual).isEqualTo(expected);
    }
}

