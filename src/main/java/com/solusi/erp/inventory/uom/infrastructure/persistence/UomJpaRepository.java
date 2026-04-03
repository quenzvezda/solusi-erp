package com.solusi.erp.inventory.uom.infrastructure.persistence;

import com.solusi.erp.inventory.uom.domain.model.UomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UomJpaRepository extends JpaRepository<UomEntity, Long> {

    @Query("SELECT u FROM UomEntity u WHERE " +
           "LOWER(u.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<UomEntity> search(@Param("keyword") String keyword, Pageable pageable);

    List<UomEntity> findByType(UomType type);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
