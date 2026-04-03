package com.solusi.erp.security.role.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.role.domain.repository.RoleRepository;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateRoleUseCaseTest {

    private final RoleRepository repository = mock(RoleRepository.class);
    private final CreateRoleUseCase useCase = new CreateRoleUseCaseImpl(repository);

    @Test
    void execute_savesRole_whenNameNotDuplicate() {
        when(repository.existsByName("ROLE_STAFF")).thenReturn(false);
        when(repository.save(any(Role.class)))
                .thenAnswer(inv -> inv.getArgument(0, Role.class));

        Role result = useCase.execute("role_staff", "desc", Set.of(1L, 2L));

        assertThat(result.getName()).isEqualTo("ROLE_STAFF");
        verify(repository).save(any(Role.class));
    }

    @Test
    void execute_throwsDomainException_whenDuplicate() {
        when(repository.existsByName("ROLE_STAFF")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("role_staff", "desc", Set.of(1L)))
                .isInstanceOf(DomainException.class);

        verify(repository, never()).save(any());
    }
}

