package com.solusi.erp.security.role.application.usecase.query;

import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.role.domain.repository.RoleRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FindRoleByIdUseCaseTest {

    private final RoleRepository repository = mock(RoleRepository.class);
    private final FindRoleByIdUseCase useCase = new FindRoleByIdUseCaseImpl(repository);

    @Test
    void execute_returnsOptionalFromRepository() {
        Role expected = Role.createNew("ROLE_USER", "desc", Set.of(1L));
        when(repository.findById(5L)).thenReturn(Optional.of(expected));

        Optional<Role> actual = useCase.execute(5L);

        assertThat(actual).contains(expected);
    }
}

