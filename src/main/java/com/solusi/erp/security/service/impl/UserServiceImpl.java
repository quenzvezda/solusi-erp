package com.solusi.erp.security.service.impl;

import com.solusi.erp.security.dto.ProfileRequest;
import com.solusi.erp.security.dto.ProfileResponse;
import com.solusi.erp.security.dto.UserRequest;
import com.solusi.erp.security.dto.UserResponse;
import com.solusi.erp.security.mapper.UserMapper;
import com.solusi.erp.security.model.Role;
import com.solusi.erp.security.model.User;
import com.solusi.erp.security.model.UserProfile;
import com.solusi.erp.security.repository.RoleRepository;
import com.solusi.erp.security.repository.UserProfileRepository;
import com.solusi.erp.security.repository.UserRepository;
import com.solusi.erp.security.service.UserService;
import lombok.RequiredArgsConstructor;
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
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserRequest getEditData(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        return userMapper.toRequest(user);
    }

    @Override
    @Transactional
    public void create(UserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username sudah digunakan");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email sudah digunakan");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role tidak ditemukan"));

        if (!StringUtils.hasText(request.getPassword()) || request.getPassword().length() < 6) {
            throw new RuntimeException("Password minimal 6 karakter");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setPasswordChangeRequired(true); // Always force change for new users

        UserProfile profile = userMapper.toProfileEntity(request);
        profile.setUser(user);
        user.setProfile(profile);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // Validate unique username/email excluding current user
        userRepository.findByUsername(request.getUsername()).ifPresent(existing -> {
            if (!existing.getId().equals(id))
                throw new RuntimeException("Username sudah digunakan");
        });
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(id))
                throw new RuntimeException("Email sudah digunakan");
        });

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role tidak ditemukan"));

        userMapper.updateEntity(request, user);
        user.setRole(role);

        if (StringUtils.hasText(request.getPassword())) {
            if (request.getPassword().length() < 6) {
                throw new RuntimeException("Password minimal 6 karakter");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userMapper.updateProfileEntity(request, user.getProfile());

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        if ("admin".equals(user.getUsername())) {
            throw new RuntimeException("User admin utama tidak boleh dihapus");
        }

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        if ("admin".equals(user.getUsername())) {
            throw new RuntimeException("Status user admin tidak boleh diubah");
        }

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileRequest getProfileUpdateData(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        UserProfile profile = user.getProfile();
        return ProfileRequest.builder()
                .fullName(profile.getFullName())
                .email(user.getEmail())
                .phoneNumber(profile.getPhoneNumber())
                .languageCode(profile.getLanguageCode())
                .defaultPageSize(profile.getDefaultPageSize())
                .theme(profile.getTheme())
                .build();
    }

    @Override
    @Transactional
    public void updateProfile(String username, ProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // Validate unique email excluding current user
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId()))
                throw new RuntimeException("Email sudah digunakan oleh pengguna lain");
        });

        user.setEmail(request.getEmail());
        userMapper.updateProfileFromRequest(request, user.getProfile());

        // Handle password change if requested
        if (StringUtils.hasText(request.getNewPassword())) {
            if (!StringUtils.hasText(request.getCurrentPassword())) {
                throw new RuntimeException("Password saat ini wajib diisi untuk mengubah password");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Password saat ini tidak sesuai");
            }
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new RuntimeException("Konfirmasi password baru tidak sesuai");
            }
            if (request.getNewPassword().length() < 6) {
                throw new RuntimeException("Password baru minimal 6 karakter");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setPasswordChangeRequired(false);
        }

        userRepository.save(user);
    }
}
