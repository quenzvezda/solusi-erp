package com.solusi.erp.master.party.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for PartyIdentificationType.
 */
@Repository
public interface PartyIdentificationTypeJpaRepository extends JpaRepository<PartyIdentificationType, Long> {
    Optional<PartyIdentificationType> findByCode(String code);
}

