package com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SupplierPriceListJpaRepository query tests")
class SupplierPriceListJpaRepositoryQueryTest {

    @Test
    @DisplayName("search query includes supplier and product name conditions")
    void searchQuery_includesSupplierAndProductName() throws Exception {
        Method method = SupplierPriceListJpaRepository.class.getMethod("search", String.class, Pageable.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(query).isNotNull();
        assertThat(query.value()).containsIgnoringCase("p.name");
        assertThat(query.value()).containsIgnoringCase("prod.name");
    }

    @Test
    @DisplayName("search query uses explicit joins for supplier and product")
    void searchQuery_usesJoinForSupplierAndProduct() throws Exception {
        Method method = SupplierPriceListJpaRepository.class.getMethod("search", String.class, Pageable.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(query).isNotNull();
        assertThat(query.value()).containsIgnoringCase("JOIN Party p");
        assertThat(query.value()).containsIgnoringCase("JOIN ProductEntity prod");
    }
    
    @Test
    @DisplayName("findAllWithJoin method exists and supports sorting")
    void findAllWithJoin_existsAndSupportsSorting() throws Exception {
        Method method = SupplierPriceListJpaRepository.class.getMethod("findAllWithJoin", Pageable.class);
        
        assertThat(method).isNotNull();
        assertThat(method.getReturnType().getName()).contains("Page");
    }
    
    @Test
    @DisplayName("findAllWithJoin query includes joins for supplier and product")
    void findAllWithJoin_includesJoinsForSupplierAndProduct() throws Exception {
        Method method = SupplierPriceListJpaRepository.class.getMethod("findAllWithJoin", Pageable.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(query).isNotNull();
        assertThat(query.value()).containsIgnoringCase("JOIN Party p");
        assertThat(query.value()).containsIgnoringCase("JOIN ProductEntity prod");
    }
}
