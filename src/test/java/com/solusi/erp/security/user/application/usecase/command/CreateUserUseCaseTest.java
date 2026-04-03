package com.solusi.erp.security.user.application.usecase.command;

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

class CreateUserUseCaseTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final PasswordCipher cipher = mock(PasswordCipher.class);
    private final PartyReferenceGateway partyGateway = mock(PartyReferenceGateway.class);
    private final CreateUserUseCase useCase = new CreateUserUseCaseImpl(repository, cipher, partyGateway);

    @Test
    void execute_savesUser_whenValid() {
        when(repository.findByUsername("john")).thenReturn(Optional.empty());
        when(repository.findByEmail("john@test.com")).thenReturn(Optional.empty());
        when(repository.roleExists(1L)).thenReturn(true);
        when(cipher.encode("secret123")).thenReturn("enc");
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0, User.class));

        User user = useCase.execute("john", "john@test.com", "secret123", 1L,
                "John Doe", "0812", true, null);

        assertThat(user.getUsername()).isEqualTo("john");
        assertThat(user.getPassword()).isEqualTo("enc");
        verify(repository).save(any(User.class));
    }

    @Test
    void execute_throws_whenRoleMissing() {
        when(repository.findByUsername(any())).thenReturn(Optional.empty());
        when(repository.findByEmail(any())).thenReturn(Optional.empty());
        when(repository.roleExists(9L)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute("john", "john@test.com", "secret123", 9L,
                "John Doe", "0812", true, null)).isInstanceOf(DomainException.class);
    }
}
