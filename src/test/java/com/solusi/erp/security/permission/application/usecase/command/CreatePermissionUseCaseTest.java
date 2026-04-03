package com.solusi.erp.security.permission.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.domain.repository.PermissionRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreatePermissionUseCaseTest {

    private final PermissionRepository repository = mock(PermissionRepository.class);
    private final CreatePermissionUseCase useCase = new CreatePermissionUseCaseImpl(repository);

    @Test
    void execute_savesPermission_whenNameNotDuplicate() {
        when(repository.existsByName("INV-READ")).thenReturn(false);
        when(repository.save(any(Permission.class)))
                .thenAnswer(inv -> inv.getArgument(0, Permission.class));

        Permission result = useCase.execute("inv read", "desc", 1L);

        assertThat(result.getName()).isEqualTo("INV-READ");
        verify(repository).save(any(Permission.class));
    }

    @Test
    void execute_throwsDomainException_whenDuplicate() {
        when(repository.existsByName("INV-READ")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("inv read", "desc", 1L))
                .isInstanceOf(DomainException.class);

        verify(repository, never()).save(any());
    }
}
