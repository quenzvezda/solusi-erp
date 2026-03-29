package com.solusi.erp.security.user.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.user.application.port.PasswordCipher;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.model.UserProfile;
import com.solusi.erp.security.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateProfileUseCaseTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final PasswordCipher cipher = mock(PasswordCipher.class);
    private final UpdateProfileUseCase useCase = new UpdateProfileUseCaseImpl(repository, cipher);

    @Test
    void execute_updatesProfile_withoutPasswordChange() {
        User user = new User(new AuditMetadata(1L, 1L, null, null, null, null), "john", "enc", "john@test.com",
                true, false, null, 1L, null, null, null, null, null, UserProfile.createDefault("John", "0812"));
        when(repository.findByUsername("john")).thenReturn(Optional.of(user));
        when(repository.findByEmail("john2@test.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0, User.class));

        User updated = useCase.execute("john", "John New", "john2@test.com", "0822", "en", 25, "dark", null, null, null);

        assertThat(updated.getEmail()).isEqualTo("john2@test.com");
        assertThat(updated.getProfile().getLanguageCode()).isEqualTo("en");
    }

    @Test
    void execute_throwsWhenCurrentPasswordInvalid() {
        User user = new User(new AuditMetadata(1L, 1L, null, null, null, null), "john", "enc", "john@test.com",
                true, false, null, 1L, null, null, null, null, null, UserProfile.createDefault("John", "0812"));
        when(repository.findByUsername("john")).thenReturn(Optional.of(user));
        when(repository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(cipher.matches("wrong", "enc")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute("john", "John", "john@test.com", "0822", "id", 10, "light",
                "wrong", "newpass", "newpass")).isInstanceOf(DomainException.class);
    }
}
