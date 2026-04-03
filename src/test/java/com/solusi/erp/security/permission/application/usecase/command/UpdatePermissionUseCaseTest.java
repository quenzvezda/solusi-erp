package com.solusi.erp.security.permission.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.domain.repository.PermissionRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdatePermissionUseCaseTest {

    private final PermissionRepository repository = mock(PermissionRepository.class);
    private final UpdatePermissionUseCase useCase = new UpdatePermissionUseCaseImpl(repository);

    @Test
    void execute_updatesPermission_whenValid() {
        Permission existing = new Permission(new AuditMetadata(1L, 1L, null, null, null, null), "INV_READ", "old", 1L);
        Permission sameName = new Permission(new AuditMetadata(1L, 1L, null, null, null, null), "INV-READ", "old", 1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByName("INV-READ")).thenReturn(Optional.of(sameName));
        when(repository.save(any(Permission.class)))
                .thenAnswer(inv -> inv.getArgument(0, Permission.class));

        Permission result = useCase.execute(1L, "inv read", "new", 2L);

        assertThat(result.getName()).isEqualTo("INV-READ");
        assertThat(result.getDescription()).isEqualTo("new");
        assertThat(result.getPermissionGroupId()).isEqualTo(2L);
    }

    @Test
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(1L, "INV_READ", "desc", 1L))
                .isInstanceOf(DomainException.class);
    }
}
