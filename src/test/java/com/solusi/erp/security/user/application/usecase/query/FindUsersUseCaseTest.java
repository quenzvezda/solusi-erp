package com.solusi.erp.security.user.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FindUsersUseCaseTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final FindUsersUseCase useCase = new FindUsersUseCaseImpl(repository);

    @Test
    void execute_returnsPageFromRepository() {
        Page<User> page = new Page<>(List.of(), 0, 10, 0);
        when(repository.findAll("john", Pageable.of(0, 10))).thenReturn(page);

        Page<User> result = useCase.execute("john", Pageable.of(0, 10));

        assertThat(result).isEqualTo(page);
    }
}
