package com.solusi.erp.inventory.goodsissue.infrastructure.service;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.port.GoodsIssueSourceResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoodsIssueSourceResolverRegistryTest {

    @Test
    void getResolver_returnsRegisteredResolver() {
        GoodsIssueSourceResolver resolver = resolver(GoodsIssueReferenceType.PURCHASE_RETURN);
        GoodsIssueSourceResolverRegistry registry = new GoodsIssueSourceResolverRegistry(List.of(resolver));

        assertThat(registry.getResolver(GoodsIssueReferenceType.PURCHASE_RETURN)).isSameAs(resolver);
    }

    @Test
    void constructor_rejectsDuplicateReferenceType() {
        assertThatThrownBy(() -> new GoodsIssueSourceResolverRegistry(List.of(
                resolver(GoodsIssueReferenceType.PURCHASE_RETURN),
                resolver(GoodsIssueReferenceType.PURCHASE_RETURN)
        )))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.common.duplicate");
    }

    @Test
    void getResolver_rejectsUnsupportedReferenceType() {
        GoodsIssueSourceResolverRegistry registry = new GoodsIssueSourceResolverRegistry(List.of());

        assertThatThrownBy(() -> registry.getResolver(GoodsIssueReferenceType.PURCHASE_RETURN))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.reference.unsupported");
    }

    private static GoodsIssueSourceResolver resolver(GoodsIssueReferenceType referenceType) {
        return new GoodsIssueSourceResolver() {
            @Override
            public GoodsIssueReferenceType getReferenceType() {
                return referenceType;
            }

            @Override
            public GoodsIssue resolve(Long referenceId) {
                return null;
            }
        };
    }
}
