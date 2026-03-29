package com.solusi.erp.security.user.application.usecase.query;

import com.solusi.erp.security.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetProfileUpdateDataUseCaseTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final GetProfileUpdateDataUseCase useCase = new GetProfileUpdateDataUseCaseImpl(repository);

    @Test
    void execute_returnsByUsername() {
        when(repository.findByUsername("jane")).thenReturn(Optional.empty());

        assertThat(useCase.execute("jane")).isEqualTo(Optional.empty());
    }
}
