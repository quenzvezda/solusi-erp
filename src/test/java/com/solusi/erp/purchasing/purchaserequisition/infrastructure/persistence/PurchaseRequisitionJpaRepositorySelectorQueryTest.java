package com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PurchaseRequisitionJpaRepository selector query tests")
class PurchaseRequisitionJpaRepositorySelectorQueryTest {

    @Test
    @DisplayName("findApprovedForPoSelector query filters approved requisitions for PO selector")
    void findApprovedForPoSelector_filtersApprovedHeaders() throws Exception {
        Method method = PurchaseRequisitionJpaRepository.class.getMethod(
                "findApprovedForPoSelector",
                Long.class,
                String.class
        );
        Query query = method.getAnnotation(Query.class);

        assertThat(query).isNotNull();
        assertThat(query.value()).containsIgnoringCase("PurchaseRequisitionStatus.APPROVED");
        assertThat(query.value()).containsIgnoringCase("pr.suggestedSupplierId = :supplierId");
        assertThat(query.value()).containsIgnoringCase("pr.code");
    }

    @Test
    @DisplayName("findApprovedLinesForPoSelector query scopes lines by approved header and PR id")
    void findApprovedLinesForPoSelector_filtersApprovedLinesByHeader() throws Exception {
        Method method = PurchaseRequisitionJpaRepository.class.getMethod(
                "findApprovedLinesForPoSelector",
                Long.class,
                String.class
        );
        Query query = method.getAnnotation(Query.class);

        assertThat(query).isNotNull();
        assertThat(query.value()).containsIgnoringCase("line.header.id = :prId");
        assertThat(query.value()).containsIgnoringCase("line.header.status");
        assertThat(query.value()).containsIgnoringCase("PurchaseRequisitionStatus.APPROVED");
    }
}
