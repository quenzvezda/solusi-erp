package com.solusi.erp.master.repository;

import com.solusi.erp.master.model.PartyIdentificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for PartyIdentificationType.
 */
@Repository
public interface PartyIdentificationTypeRepository extends JpaRepository<PartyIdentificationType, Long> {
    Optional<PartyIdentificationType> findByCode(String code);
}
