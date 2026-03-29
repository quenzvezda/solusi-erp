package com.solusi.erp.security.user.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.security.role.infrastructure.persistence.Role;
import com.solusi.erp.security.role.infrastructure.persistence.RoleJpaRepository;
import com.solusi.erp.security.user.infrastructure.persistence.UserProfile;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.repository.UserRepository;
import com.solusi.erp.security.user.infrastructure.persistence.UserJpaRepository;
import com.solusi.erp.security.user.infrastructure.persistence.UserPersistenceMapper;

import java.util.Optional;

public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository userJpaRepository;
    private final RoleJpaRepository roleRepository;
    private final UserPersistenceMapper mapper;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository,
                                 RoleJpaRepository roleRepository,
                                 UserPersistenceMapper mapper) {
        this.userJpaRepository = userJpaRepository;
        this.roleRepository = roleRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<User> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Page<com.solusi.erp.security.user.infrastructure.persistence.User> result;
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        if (keyword != null && !keyword.isBlank()) {
            result = userJpaRepository.search(keyword, springPageable);
        } else {
            result = userJpaRepository.findAll(springPageable);
        }
        return new Page<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        com.solusi.erp.security.user.infrastructure.persistence.User entity = mapper.toEntity(user);
        Role role = roleRepository.findById(user.getRoleId()).orElseThrow(() -> new RuntimeException("Role not found"));
        entity.setRole(role);

        UserProfile profile = mapper.toEntityProfile(user.getProfile());
        if (profile != null) {
            if (entity.getId() != null) {
                userJpaRepository.findById(entity.getId()).ifPresent(existing -> {
                    if (existing.getProfile() != null) {
                        profile.setId(existing.getProfile().getId());
                    }
                });
            }
            profile.setUser(entity);
            entity.setProfile(profile);
        }

        com.solusi.erp.security.user.infrastructure.persistence.User saved = userJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }

    @Override
    public boolean roleExists(Long roleId) {
        return roleRepository.existsById(roleId);
    }
}
