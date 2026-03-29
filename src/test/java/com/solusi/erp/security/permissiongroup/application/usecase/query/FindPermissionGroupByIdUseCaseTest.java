package com.solusi.erp.security.permissiongroup.application.usecase.query;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import com.solusi.erp.security.permissiongroup.domain.repository.PermissionGroupRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FindPermissionGroupByIdUseCaseTest {

    private final PermissionGroupRepository repository = mock(PermissionGroupRepository.class);
    private final FindPermissionGroupByIdUseCase useCase = new FindPermissionGroupByIdUseCaseImpl(repository);

    @Test
    void execute_returnsGroup_whenFound() {
        PermissionGroup pg = new PermissionGroup(new AuditMetadata(1L, 1L, null, null, null, null),
                "CODE", "N", "N", "B", "B", "/u", null, null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(pg));

        Optional<PermissionGroup> result = useCase.execute(1L);

        assertThat(result).contains(pg);
    }

    @Test
    void execute_returnsEmpty_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThat(useCase.execute(99L)).isEmpty();
    }
}
