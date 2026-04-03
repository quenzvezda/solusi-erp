package com.solusi.erp.security.user.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.user.application.port.PasswordCipher;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.model.UserProfile;
import com.solusi.erp.security.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("UpdateProfileUseCase Tests")
class UpdateProfileUseCaseTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final PasswordCipher cipher = mock(PasswordCipher.class);
    private final UpdateProfileUseCase useCase = new UpdateProfileUseCaseImpl(repository, cipher);

    private User userWithProfile(String username, String password) {
        return new User(new AuditMetadata(1L, 1L, null, null, null, null),
                username, password, username + "@test.com",
                true, false, null, 1L, null, null, null, null, null,
                UserProfile.createDefault("John", "0812"));
    }

    private User userWithoutProfile(String username) {
        return new User(new AuditMetadata(2L, 1L, null, null, null, null),
                username, "enc", username + "@test.com",
                true, false, null, 1L, null, null, null, null, null, null);
    }

    @Test
    @DisplayName("successfully updates profile fields without password change")
    void execute_updatesProfile_withoutPasswordChange() {
        User user = userWithProfile("john", "enc");
        when(repository.findByUsername("john")).thenReturn(Optional.of(user));
        when(repository.findByEmail("john2@test.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0, User.class));

        User updated = useCase.execute("john", "John New", "john2@test.com", "0822",
                "en", 25, "dark", null, null, null);

        assertThat(updated.getEmail()).isEqualTo("john2@test.com");
        assertThat(updated.getProfile().getLanguageCode()).isEqualTo("en");
    }

    @Test
    @DisplayName("throws DomainException when user is not found")
    void execute_throws_whenUserNotFound() {
        when(repository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("ghost", "Name", "a@b.com", "0811",
                "id", 10, "light", null, null, null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("throws DomainException when email belongs to another user")
    void execute_throws_whenEmailExistsOnAnotherUser() {
        User user = userWithProfile("john", "enc");
        User otherUser = userWithProfile("jane", "enc");
        // otherUser has a different id (2L vs 1L)
        User anotherUser = new User(new AuditMetadata(99L, 1L, null, null, null, null),
                "other", "enc", "taken@test.com",
                true, false, null, 1L, null, null, null, null, null, null);

        when(repository.findByUsername("john")).thenReturn(Optional.of(user));
        when(repository.findByEmail("taken@test.com")).thenReturn(Optional.of(anotherUser));

        assertThatThrownBy(() -> useCase.execute("john", "John", "taken@test.com", "0822",
                "id", 10, "light", null, null, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("email-exists");
    }

    @Test
    @DisplayName("creates a new UserProfile when user has no profile yet")
    void execute_createsNewProfile_whenProfileIsNull() {
        User user = userWithoutProfile("newuser");
        when(repository.findByUsername("newuser")).thenReturn(Optional.of(user));
        when(repository.findByEmail("newuser@test.com")).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = useCase.execute("newuser", "New User", "newuser@test.com", "0811",
                "en", 20, "dark", null, null, null);

        assertThat(updated.getProfile()).isNotNull();
        assertThat(updated.getProfile().getFullName()).isEqualTo("New User");
    }

    @Test
    @DisplayName("throws DomainException when new password given but current password is blank")
    void execute_throws_whenCurrentPasswordIsBlank() {
        User user = userWithProfile("john", "enc");
        when(repository.findByUsername("john")).thenReturn(Optional.of(user));
        when(repository.findByEmail("john@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> useCase.execute("john", "John", "john@test.com", "0822",
                "id", 10, "light", "  ", "newpass123", "newpass123"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("current-password-required");
    }

    @Test
    @DisplayName("throws DomainException when current password does not match")
    void execute_throws_whenCurrentPasswordMismatch() {
        User user = userWithProfile("john", "enc");
        when(repository.findByUsername("john")).thenReturn(Optional.of(user));
        when(repository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(cipher.matches("wrong", "enc")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute("john", "John", "john@test.com", "0822",
                "id", 10, "light", "wrong", "newpass123", "newpass123"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("current-password-mismatch");
    }

    @Test
    @DisplayName("throws DomainException when confirmation password does not match new password")
    void execute_throws_whenConfirmPasswordMismatch() {
        User user = userWithProfile("john", "enc");
        when(repository.findByUsername("john")).thenReturn(Optional.of(user));
        when(repository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(cipher.matches("correct", "enc")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("john", "John", "john@test.com", "0822",
                "id", 10, "light", "correct", "newpass123", "different"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("confirm-password-mismatch");
    }

    @Test
    @DisplayName("throws DomainException when new password is shorter than 6 characters")
    void execute_throws_whenPasswordTooShort() {
        User user = userWithProfile("john", "enc");
        when(repository.findByUsername("john")).thenReturn(Optional.of(user));
        when(repository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(cipher.matches("correct", "enc")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("john", "John", "john@test.com", "0822",
                "id", 10, "light", "correct", "abc", "abc"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("password-too-short");
    }

    @Test
    @DisplayName("successfully updates password when all validations pass")
    void execute_updatesPassword_whenAllValidationsPasses() {
        User user = userWithProfile("john", "enc");
        when(repository.findByUsername("john")).thenReturn(Optional.of(user));
        when(repository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(cipher.matches("correct", "enc")).thenReturn(true);
        when(cipher.encode("newpass123")).thenReturn("newEncodedPass");
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = useCase.execute("john", "John", "john@test.com", "0822",
                "id", 10, "light", "correct", "newpass123", "newpass123");

        assertThat(updated.getPassword()).isEqualTo("newEncodedPass");
        verify(cipher).encode("newpass123");
    }
}
