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
        return page.map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.user.notfound")));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserRequest getEditData(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.user.notfound")));

        return userMapper.toRequest(user);
    }

    @Override
    @Transactional(readOnly = true)
    public FormViewDto<UserRequest, UserUIForm, UserResponse> getUserEditView(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.user.notfound")));

        return FormViewDto.<UserRequest, UserUIForm, UserResponse>builder()
                .request(userMapper.toRequest(user))
                .ui(userMapper.toUIForm(user))
                .audit(userMapper.toResponse(user))
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

        if (StringUtils.hasText(request.getPassword())) {
            if (request.getPassword().length() < 6) {
                throw new RuntimeException(getMessage("msg.error.user.password-too-short"));
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userMapper.updateProfileEntity(request, user.getProfile());

        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
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
        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileRequest getProfileUpdateData(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.user.notfound")));

        return userMapper.toProfileRequest(user);
    }

    @Override
    @Transactional
    public void updateProfile(String username, ProfileRequest request) {
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

        userRepository.save(user);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
