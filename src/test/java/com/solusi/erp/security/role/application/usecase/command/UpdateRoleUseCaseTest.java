package com.solusi.erp.security.role.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.role.domain.repository.RoleRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateRoleUseCaseTest {

    private final RoleRepository repository = mock(RoleRepository.class);
    private final UpdateRoleUseCase useCase = new UpdateRoleUseCaseImpl(repository);

    @Test
    void execute_updatesRole_whenValid() {
        Role existing = new Role(new AuditMetadata(1L, 1L, null, null, null, null), "ROLE_OLD", "old", Set.of(1L));
        Role sameName = new Role(new AuditMetadata(1L, 1L, null, null, null, null), "ROLE_MANAGER", "old", Set.of(1L));
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByName("ROLE_MANAGER")).thenReturn(Optional.of(sameName));
        when(repository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0, Role.class));

        Role result = useCase.execute(1L, "role_manager", "new", Set.of(2L, 3L));

        assertThat(result.getName()).isEqualTo("ROLE_MANAGER");
        assertThat(result.getDescription()).isEqualTo("new");
        assertThat(result.getPermissionIds()).containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(1L, "ROLE_MANAGER", "desc", Set.of(1L)))
                .isInstanceOf(DomainException.class);
    }
}

