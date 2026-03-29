package com.solusi.erp.security.user.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.user.application.port.PartyReferenceGateway;
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

class UpdateUserUseCaseTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final PasswordCipher cipher = mock(PasswordCipher.class);
    private final PartyReferenceGateway partyGateway = mock(PartyReferenceGateway.class);
    private final UpdateUserUseCase useCase = new UpdateUserUseCaseImpl(repository, cipher, partyGateway);

    @Test
    void execute_updatesAndSaves() {
        User user = new User(new AuditMetadata(1L, 1L, null, null, null, null), "john", "enc", "john@test.com",
                true, false, null, 1L, null, null, null, null, null, UserProfile.createDefault("John", "0812"));
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.findByUsername("john-new")).thenReturn(Optional.empty());
        when(repository.findByEmail("john2@test.com")).thenReturn(Optional.empty());
        when(repository.roleExists(1L)).thenReturn(true);
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0, User.class));

        User updated = useCase.execute(1L, "john-new", "john2@test.com", "", 1L,
                "John New", "0833", true, false, null);

        assertThat(updated.getUsername()).isEqualTo("john-new");
        assertThat(updated.getEmail()).isEqualTo("john2@test.com");
    }

    @Test
    void execute_throwsWhenPartyImmutableViolated() {
        User user = new User(new AuditMetadata(1L, 1L, null, null, null, null), "john", "enc", "john@test.com",
                true, false, null, 1L, null, null, 10L, null, null, UserProfile.createDefault("John", "0812"));
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.findByUsername("john")).thenReturn(Optional.of(user));
        when(repository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(repository.roleExists(1L)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(1L, "john", "john@test.com", "", 1L,
                "John", "0812", true, false, 11L)).isInstanceOf(DomainException.class);
    }
}
