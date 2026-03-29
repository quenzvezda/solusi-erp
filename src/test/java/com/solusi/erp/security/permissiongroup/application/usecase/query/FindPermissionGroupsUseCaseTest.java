package com.solusi.erp.security.permissiongroup.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import com.solusi.erp.security.permissiongroup.domain.repository.PermissionGroupRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FindPermissionGroupsUseCaseTest {

    private final PermissionGroupRepository repository = mock(PermissionGroupRepository.class);
    private final FindPermissionGroupsUseCase useCase = new FindPermissionGroupsUseCaseImpl(repository);

    @Test
    void execute_delegatesToRepository() {
        Pageable pageable = new Pageable(0, 20, "code", "ASC");
        Page<PermissionGroup> expected = new Page<>(List.of(), 0, 20, 0L);
        when(repository.findAll("kw", pageable)).thenReturn(expected);

        Page<PermissionGroup> result = useCase.execute("kw", pageable);

        assertThat(result).isSameAs(expected);
        verify(repository).findAll("kw", pageable);
    }
}
