package com.solusi.erp.security.permissiongroup.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import com.solusi.erp.security.permissiongroup.domain.repository.PermissionGroupRepository;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreatePermissionGroupUseCaseTest {

    private final PermissionGroupRepository repository = mock(PermissionGroupRepository.class);
    private final CreatePermissionGroupUseCase useCase = new CreatePermissionGroupUseCaseImpl(repository);

    @Test
    void execute_savesAndReturnsGroup_whenCodeNotDuplicate() {
        when(repository.existsByCode("SEC-01")).thenReturn(false);
        PermissionGroup saved = PermissionGroup.createNew("SEC-01", "N", "N", "B", "B", "/u", null, null, null);
        when(repository.save(any())).thenReturn(saved);

        PermissionGroup result = useCase.execute("SEC-01", "N", "N", "B", "B", "/u", null, null, null);

        assertThat(result).isNotNull();
        verify(repository).save(any(PermissionGroup.class));
    }

    @Test
    void execute_throwsDomainException_whenCodeIsDuplicate() {
        when(repository.existsByCode("DUP")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("DUP", "N", "N", "B", "B", "/u", null, null, null))
                .isInstanceOf(DomainException.class);

        verify(repository, never()).save(any());
    }
}
