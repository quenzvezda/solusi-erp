package com.solusi.erp.security.user.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.model.UserProfile;
import com.solusi.erp.security.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DeleteUserUseCaseTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final DeleteUserUseCase useCase = new DeleteUserUseCaseImpl(repository);

    @Test
    void execute_deletes_whenNotAdmin() {
        User user = new User(new AuditMetadata(2L, 1L, null, null, null, null), "john", "enc", "john@test.com",
                true, false, null, 1L, null, null, null, null, null, UserProfile.createDefault("John", "0812"));
        when(repository.findById(2L)).thenReturn(Optional.of(user));

        useCase.execute(2L);

        verify(repository).deleteById(2L);
    }

    @Test
    void execute_throws_whenAdmin() {
        User user = new User(new AuditMetadata(1L, 1L, null, null, null, null), "admin", "enc", "admin@test.com",
                true, false, null, 1L, null, null, null, null, null, UserProfile.createDefault("Admin", "0812"));
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> useCase.execute(1L)).isInstanceOf(DomainException.class);
    }
}
