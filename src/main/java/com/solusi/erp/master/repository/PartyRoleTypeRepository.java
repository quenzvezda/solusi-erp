package com.solusi.erp.master.repository;

import com.solusi.erp.master.model.PartyRoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for PartyRoleType.
 */
@Repository
public interface PartyRoleTypeRepository extends JpaRepository<PartyRoleType, Long> {
    Optional<PartyRoleType> findByCode(String code);
}
