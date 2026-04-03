package com.solusi.erp.security.permissiongroup.application.usecase.command;

import com.solusi.erp.security.permissiongroup.domain.repository.PermissionGroupRepository;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class DeletePermissionGroupUseCaseTest {

    private final PermissionGroupRepository repository = mock(PermissionGroupRepository.class);
    private final DeletePermissionGroupUseCase useCase = new DeletePermissionGroupUseCaseImpl(repository);

    @Test
    void execute_delegatesToRepository() {
        useCase.execute(1L);
        verify(repository).delete(1L);
    }
}
