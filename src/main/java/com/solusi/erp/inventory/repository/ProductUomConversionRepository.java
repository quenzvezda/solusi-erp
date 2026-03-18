package com.solusi.erp.inventory.repository;

import com.solusi.erp.inventory.model.ProductUomConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductUomConversionRepository extends JpaRepository<ProductUomConversion, Long> {
    
    Optional<ProductUomConversion> findByProductIdAndFromUomIdAndToUomId(Long productId, Long fromUomId, Long toUomId);
}
