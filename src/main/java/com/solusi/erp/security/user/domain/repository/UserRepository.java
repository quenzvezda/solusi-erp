package com.solusi.erp.security.user.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.security.user.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    Page<User> findAll(String keyword, Pageable pageable);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPartyId(Long partyId);

    User save(User user);

    void deleteById(Long id);

    boolean roleExists(Long roleId);
}

