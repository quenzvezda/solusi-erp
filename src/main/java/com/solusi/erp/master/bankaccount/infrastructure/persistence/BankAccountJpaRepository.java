package com.solusi.erp.master.bankaccount.infrastructure.persistence;

import com.solusi.erp.master.model.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BankAccountJpaRepository extends JpaRepository<BankAccount, Long>, JpaSpecificationExecutor<BankAccount> {

    Page<BankAccount> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT b FROM BankAccount b WHERE b.isActive = true AND (LOWER(b.bankName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.accountNo) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.code) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.accountName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<BankAccount> search(@Param("search") String search, Pageable pageable);

    Optional<BankAccount> findByIdAndIsActiveTrue(Long id);
}

