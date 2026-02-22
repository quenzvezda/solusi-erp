package com.solusi.erp.security.repository;

import com.solusi.erp.security.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    /**
     * Find profile by the associated user id.
     * 
     * @param userId The ID of the User.
     * @return Optional UserProfile.
     */
    Optional<UserProfile> findByUserId(Long userId);
}
