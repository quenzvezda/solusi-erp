package com.solusi.erp.master.repository;

import com.solusi.erp.master.model.Geographic;
import com.solusi.erp.master.model.GeographicType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeographicRepository extends JpaRepository<Geographic, Long> {

    @Query("SELECT g FROM Geographic g WHERE " +
           "LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Geographic> search(@Param("keyword") String keyword, Pageable pageable);

    Page<Geographic> findByParentId(Long parentId, Pageable pageable);

    List<Geographic> findByTypeAndIsActiveTrue(GeographicType type);
    
    List<Geographic> findByParentIdAndIsActiveTrue(Long parentId);
}
