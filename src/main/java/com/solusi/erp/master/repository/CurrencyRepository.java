package com.solusi.erp.master.repository;

import com.solusi.erp.master.model.Currency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    @Query("SELECT c FROM Currency c WHERE LOWER(c.symbol) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.alias) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Currency> search(@Param("keyword") String keyword, Pageable pageable);

    Optional<Currency> findByAlias(String alias);

    List<Currency> findByIsDefaultTrue();
}
