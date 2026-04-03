package com.solusi.erp.security.permissiongroup.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import com.solusi.erp.security.permissiongroup.domain.repository.PermissionGroupRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdatePermissionGroupUseCaseTest {

    private final PermissionGroupRepository repository = mock(PermissionGroupRepository.class);
    private final UpdatePermissionGroupUseCase useCase = new UpdatePermissionGroupUseCaseImpl(repository);

    @Test
    void execute_updatesAndReturnsGroup_whenFound() {
        PermissionGroup existing = new PermissionGroup(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "SEC-01", "Old", "Old", "B", "B", "/old", null, null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PermissionGroup result = useCase.execute(1L, "New", "New", "B2", "B2", "/new", "ti-lock", null, null);

        assertThat(result.getNameId()).isEqualTo("New");
        assertThat(result.getUrlPath()).isEqualTo("/new");
        verify(repository).save(existing);
    }

    @Test
    void execute_throwsException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, "N", "N", "B", "B", "/u", null, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }
}
