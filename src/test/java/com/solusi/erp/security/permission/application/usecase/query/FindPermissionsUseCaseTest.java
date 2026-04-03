package com.solusi.erp.security.permission.application.usecase.query;

import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.domain.repository.PermissionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FindPermissionsUseCaseTest {

    private final PermissionRepository repository = mock(PermissionRepository.class);
    private final FindPermissionsUseCase useCase = new FindPermissionsUseCaseImpl(repository);

    @Test
    void execute_returnsRepositoryResult() {
        List<Permission> expected = List.of(Permission.createNew("INV_READ", "desc", 1L));
        when(repository.findAll()).thenReturn(expected);

        List<Permission> actual = useCase.execute();

        assertThat(actual).isEqualTo(expected);
    }
}
