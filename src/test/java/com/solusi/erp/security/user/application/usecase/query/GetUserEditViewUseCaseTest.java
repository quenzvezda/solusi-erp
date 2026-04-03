package com.solusi.erp.security.user.application.usecase.query;

import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetUserEditViewUseCaseTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final GetUserEditViewUseCase useCase = new GetUserEditViewUseCaseImpl(repository);

    @Test
    void execute_returnsRepositoryResult() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThat(useCase.execute(7L)).isEmpty();
    }
}
