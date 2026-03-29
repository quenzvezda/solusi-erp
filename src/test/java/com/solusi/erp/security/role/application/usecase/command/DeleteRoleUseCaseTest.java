package com.solusi.erp.security.role.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.role.domain.repository.RoleRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DeleteRoleUseCaseTest {

    private final RoleRepository repository = mock(RoleRepository.class);
    private final DeleteRoleUseCase useCase = new DeleteRoleUseCaseImpl(repository);

    @Test
    void execute_deletesRole_whenNotAdmin() {
        Role role = new Role(new AuditMetadata(1L, 1L, null, null, null, null), "ROLE_USER", null, Set.of());
        when(repository.findById(1L)).thenReturn(Optional.of(role));

        useCase.execute(1L);

        verify(repository).delete(role);
    }

    @Test
    void execute_throwsDomainException_whenAdminRole() {
        Role role = new Role(new AuditMetadata(1L, 1L, null, null, null, null), "ROLE_ADMIN", null, Set.of());
        when(repository.findById(1L)).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(DomainException.class);

        verify(repository, never()).delete(any());
    }
}

