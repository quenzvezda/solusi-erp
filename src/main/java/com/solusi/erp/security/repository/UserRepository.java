package com.solusi.erp.security.repository;

import com.solusi.erp.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds a user by their username for authentication.
     * 
     * @param username The username to find.
     * @return Optional User.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     * 
     * @param email The email to find.
     * @return Optional User.
     */
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
