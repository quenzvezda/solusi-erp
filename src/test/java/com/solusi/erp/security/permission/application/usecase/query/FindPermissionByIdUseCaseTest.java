package com.solusi.erp.security.permission.application.usecase.query;

import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.domain.repository.PermissionRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FindPermissionByIdUseCaseTest {

    private final PermissionRepository repository = mock(PermissionRepository.class);
    private final FindPermissionByIdUseCase useCase = new FindPermissionByIdUseCaseImpl(repository);

    @Test
    void execute_returnsOptionalFromRepository() {
        Permission expected = Permission.createNew("INV_READ", "desc", 1L);
        when(repository.findById(5L)).thenReturn(Optional.of(expected));

        Optional<Permission> actual = useCase.execute(5L);

        assertThat(actual).contains(expected);
    }
}
