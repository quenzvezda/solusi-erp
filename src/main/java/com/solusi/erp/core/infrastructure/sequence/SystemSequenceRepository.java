package com.solusi.erp.core.infrastructure.sequence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for SystemSequence.
 */
@Repository
public interface SystemSequenceRepository extends JpaRepository<SystemSequence, String> {
}
