package com.solusi.erp.security.service.impl;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.security.dto.ProfileRequest;
import com.solusi.erp.security.dto.ProfileResponse;
import com.solusi.erp.security.dto.UserRequest;
import com.solusi.erp.security.dto.UserResponse;
import com.solusi.erp.security.form.UserUIForm;
import com.solusi.erp.security.mapper.UserMapper;
import com.solusi.erp.security.model.Role;
import com.solusi.erp.security.model.User;
import com.solusi.erp.security.model.UserProfile;
import com.solusi.erp.security.repository.RoleRepository;
import com.solusi.erp.master.party.application.usecase.query.GetPartyEditViewUseCase;
import com.solusi.erp.master.party.domain.model.Party;
import com.solusi.erp.security.repository.UserRepository;
import com.solusi.erp.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GetPartyEditViewUseCase getPartyEditViewUseCase;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(String keyword, Pageable pageable) {
        Page<User> page;
        if (StringUtils.hasText(keyword)) {
            page = userRepository.search(keyword, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }
        return page.map(user -> userMapper.toResponse(enrichPartyInfo(user)));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.user.notfound")));
        return userMapper.toResponse(enrichPartyInfo(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserRequest getEditData(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.user.notfound")));

        return userMapper.toRequest(enrichPartyInfo(user));
    }

    @Override
    @Transactional(readOnly = true)
    public FormViewDto<UserRequest, UserUIForm, UserResponse> getUserEditView(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.user.notfound")));

        return FormViewDto.<UserRequest, UserUIForm, UserResponse>builder()
                .request(userMapper.toRequest(enrichPartyInfo(user)))
                .ui(userMapper.toUIForm(enrichPartyInfo(user)))
                .audit(userMapper.toResponse(enrichPartyInfo(user)))
                .build();
    }

    @Override
    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException(getMessage("msg.error.user.username-exists"));
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException(getMessage("msg.error.user.email-exists"));
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.role.notfound")));

        if (!StringUtils.hasText(request.getPassword()) || request.getPassword().length() < 6) {
            throw new RuntimeException(getMessage("msg.error.user.password-too-short"));
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setPasswordChangeRequired(true); // Always force change for new users

        applyPartyReference(user, request.getPartyId());

        UserProfile profile = userMapper.toProfileEntity(request);
        profile.setUser(user);
        user.setProfile(profile);

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.user.notfound")));

        // Validate unique username/email excluding current user
        userRepository.findByUsername(request.getUsername()).ifPresent(existing -> {
            if (!existing.getId().equals(id))
                throw new RuntimeException(getMessage("msg.error.user.username-exists"));
        });
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(id))
                throw new RuntimeException(getMessage("msg.error.user.email-exists"));
        });

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.role.notfound")));

        userMapper.updateEntity(request, user);
        user.setRole(role);

        // Party immutability logic
        if (user.getPartyId() != null) {
            if (request.getPartyId() != null && !request.getPartyId().equals(user.getPartyId())) {
                throw new RuntimeException(getMessage("msg.error.user.party-immutable"));
            }
            // If request.getPartyId() is null, we keep the current party (immutable)
        } else if (request.getPartyId() != null) {
            // Party can be set for the first time
            applyPartyReference(user, request.getPartyId());
        }

        if (StringUtils.hasText(request.getPassword())) {
            if (request.getPassword().length() < 6) {
                throw new RuntimeException(getMessage("msg.error.user.password-too-short"));
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userMapper.updateProfileEntity(request, user.getProfile());

        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(enrichPartyInfo(updatedUser));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.user.notfound")));

        if ("admin".equals(user.getUsername())) {
            throw new RuntimeException(getMessage("msg.error.user.delete-admin"));
        }

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.user.notfound")));

        if ("admin".equals(user.getUsername())) {
            throw new RuntimeException(getMessage("msg.error.user.toggle-admin"));
        }

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.user.notfound")));
        return userMapper.toProfileResponse(enrichPartyInfo(user));
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileRequest getProfileUpdateData(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.user.notfound")));

        return userMapper.toProfileRequest(enrichPartyInfo(user));
    }

    @Override
    @Transactional(readOnly = true)
    public FormViewDto<ProfileRequest, Void, ProfileResponse> getProfileEditView(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.user.notfound")));

        return FormViewDto.<ProfileRequest, Void, ProfileResponse>builder()
                .request(userMapper.toProfileRequest(user))
                .audit(userMapper.toProfileResponse(user))
                .build();
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(String username, ProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.user.notfound")));

        // Validate unique email excluding current user
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId()))
                throw new RuntimeException(getMessage("msg.error.user.email-exists"));
        });

        user.setEmail(request.getEmail());
        userMapper.updateProfileFromRequest(request, user.getProfile());

        // Handle password change if requested
        if (StringUtils.hasText(request.getNewPassword())) {
            if (!StringUtils.hasText(request.getCurrentPassword())) {
                throw new RuntimeException(getMessage("msg.error.user.current-password-required"));
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException(getMessage("msg.error.user.current-password-mismatch"));
            }
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new RuntimeException(getMessage("msg.error.user.confirm-password-mismatch"));
            }
            if (request.getNewPassword().length() < 6) {
                throw new RuntimeException(getMessage("msg.error.user.password-too-short"));
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setPasswordChangeRequired(false);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toProfileResponse(enrichPartyInfo(savedUser));
    }

    private User enrichPartyInfo(User user) {
        if (user.getPartyId() == null) {
            user.setPartyCode(null);
            user.setPartyName(null);
            return user;
        }

        Party party = getPartyEditViewUseCase.execute(user.getPartyId())
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.party.notfound")));
        user.setPartyCode(party.getCode());
        user.setPartyName(party.getName());
        return user;
    }

    private void applyPartyReference(User user, Long partyId) {
        if (partyId == null) {
            user.setPartyId(null);
            user.setPartyCode(null);
            user.setPartyName(null);
            return;
        }

        Party party = getPartyEditViewUseCase.execute(partyId)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.party.notfound")));
        user.setPartyId(party.getMetadata().id());
        user.setPartyCode(party.getCode());
        user.setPartyName(party.getName());
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}


