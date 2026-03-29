package com.solusi.erp.security.permission.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.domain.repository.PermissionRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DeletePermissionUseCaseTest {

    private final PermissionRepository repository = mock(PermissionRepository.class);
    private final DeletePermissionUseCase useCase = new DeletePermissionUseCaseImpl(repository);

    @Test
    void execute_deletesPermission_whenNotSystemPermission() {
        Permission permission = new Permission(new AuditMetadata(1L, 1L, null, null, null, null), "INV_READ", null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(permission));

        useCase.execute(1L);

        verify(repository).delete(permission);
    }

    @Test
    void execute_throwsDomainException_whenSystemPermission() {
        Permission permission = new Permission(new AuditMetadata(1L, 1L, null, null, null, null), "USERS_READ", null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(permission));

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(DomainException.class);

        verify(repository, never()).delete(any());
    }
}
