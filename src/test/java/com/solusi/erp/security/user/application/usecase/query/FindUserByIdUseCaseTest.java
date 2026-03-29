package com.solusi.erp.security.user.application.usecase.query;

import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FindUserByIdUseCaseTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final FindUserByIdUseCase useCase = new FindUserByIdUseCaseImpl(repository);

    @Test
    void execute_delegatesToRepository() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Optional<User> result = useCase.execute(1L);

        assertThat(result).isEmpty();
    }
}
