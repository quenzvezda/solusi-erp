package com.solusi.erp.security.user.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.user.application.port.PartyReferenceGateway;
import com.solusi.erp.security.user.application.port.PasswordCipher;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.repository.UserRepository;

public class UpdateUserUseCaseImpl implements UpdateUserUseCase {
    private final UserRepository repository;
    private final PasswordCipher passwordCipher;
    private final PartyReferenceGateway partyGateway;

    public UpdateUserUseCaseImpl(UserRepository repository,
                                 PasswordCipher passwordCipher,
                                 PartyReferenceGateway partyGateway) {
        this.repository = repository;
        this.passwordCipher = passwordCipher;
        this.partyGateway = partyGateway;
    }

    @Override
    public User execute(Long id,
                        String username,
                        String email,
                        String rawPassword,
                        Long roleId,
                        String fullName,
                        String phoneNumber,
                        Boolean enabled,
                        Boolean passwordChangeRequired,
                        Long partyId) {
        User user = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.user.notfound"));

        repository.findByUsername(username).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DomainException("msg.error.user.username-exists");
            }
        });
        repository.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DomainException("msg.error.user.email-exists");
            }
        });

        if (!repository.roleExists(roleId)) {
            throw new DomainException("msg.error.role.notfound");
        }

        if (user.getPartyId() != null && partyId != null && !partyId.equals(user.getPartyId())) {
            throw new DomainException("msg.error.user.party-immutable");
        }

        user.updateFromForm(username, email, roleId, fullName, phoneNumber, enabled, passwordChangeRequired);

        if (user.getPartyId() == null && partyId != null) {
            PartyReferenceGateway.PartyReferenceData party = partyGateway.findById(partyId)
                    .orElseThrow(() -> new DomainException("msg.error.party.notfound"));
            user.setPartyReference(party.id(), party.code(), party.name());
        }

        if (rawPassword != null && !rawPassword.isBlank()) {
            if (rawPassword.length() < 6) {
                throw new DomainException("msg.error.user.password-too-short");
            }
            user.updatePassword(passwordCipher.encode(rawPassword), user.isPasswordChangeRequired());
        }

        return repository.save(user);
    }
}
