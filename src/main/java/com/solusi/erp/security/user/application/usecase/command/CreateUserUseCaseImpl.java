package com.solusi.erp.security.user.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.user.application.port.PartyReferenceGateway;
import com.solusi.erp.security.user.application.port.PasswordCipher;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.model.UserProfile;
import com.solusi.erp.security.user.domain.repository.UserRepository;

public class CreateUserUseCaseImpl implements CreateUserUseCase {
    private final UserRepository repository;
    private final PasswordCipher passwordCipher;
    private final PartyReferenceGateway partyGateway;

    public CreateUserUseCaseImpl(UserRepository repository,
                                 PasswordCipher passwordCipher,
                                 PartyReferenceGateway partyGateway) {
        this.repository = repository;
        this.passwordCipher = passwordCipher;
        this.partyGateway = partyGateway;
    }

    @Override
    public User execute(String username,
                        String email,
                        String rawPassword,
                        Long roleId,
                        String fullName,
                        String phoneNumber,
                        Boolean enabled,
                        Long partyId) {
        if (repository.findByUsername(username).isPresent()) {
            throw new DomainException("msg.error.user.username-exists");
        }
        if (repository.findByEmail(email).isPresent()) {
            throw new DomainException("msg.error.user.email-exists");
        }
        if (!repository.roleExists(roleId)) {
            throw new DomainException("msg.error.role.notfound");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new DomainException("msg.error.user.password-too-short");
        }

        User user = User.createNew(
                username,
                passwordCipher.encode(rawPassword),
                email,
                roleId,
                UserProfile.createDefault(fullName, phoneNumber),
                enabled == null || enabled,
                partyId);

        applyPartyReference(user, partyId);
        return repository.save(user);
    }

    private void applyPartyReference(User user, Long partyId) {
        if (partyId == null) {
            user.setPartyReference(null, null, null);
            return;
        }
        PartyReferenceGateway.PartyReferenceData party = partyGateway.findById(partyId)
                .orElseThrow(() -> new DomainException("msg.error.party.notfound"));
        user.setPartyReference(party.id(), party.code(), party.name());
    }
}
