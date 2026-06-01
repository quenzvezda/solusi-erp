package com.solusi.erp.inventory.goodsissue.application.usecase.query;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssuePartyType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.port.GoodsIssueSourceResolver;
import com.solusi.erp.inventory.goodsissue.infrastructure.service.GoodsIssueSourceResolverRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetGoodsIssueCreateViewUseCaseTest {

    @Test
    void execute_whenReferenceProvided_returnsResolverDraft() {
        GoodsIssue expected = GoodsIssue.createNew(
                null,
                LocalDate.of(2026, 6, 1),
                GoodsIssueReferenceType.PURCHASE_RETURN,
                7L,
                "PRTN-0007",
                11L,
                GoodsIssuePartyType.SUPPLIER,
                3L,
                1L,
                BigDecimal.ONE,
                List.of()
        );
        GetGoodsIssueCreateViewUseCase useCase = new GetGoodsIssueCreateViewUseCaseImpl(
                new GoodsIssueSourceResolverRegistry(List.of(resolver(GoodsIssueReferenceType.PURCHASE_RETURN, expected)))
        );

        GoodsIssue result = useCase.execute(GoodsIssueReferenceType.PURCHASE_RETURN, 7L);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void execute_whenReferenceTypeUnsupported_throws() {
        GetGoodsIssueCreateViewUseCase useCase = new GetGoodsIssueCreateViewUseCaseImpl(
                new GoodsIssueSourceResolverRegistry(List.of())
        );

        assertThatThrownBy(() -> useCase.execute(GoodsIssueReferenceType.PURCHASE_RETURN, 7L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.reference.unsupported");
    }

    @Test
    void execute_whenReferenceTypeProvidedWithoutReferenceId_throws() {
        GetGoodsIssueCreateViewUseCase useCase = new GetGoodsIssueCreateViewUseCaseImpl(
                new GoodsIssueSourceResolverRegistry(List.of())
        );

        assertThatThrownBy(() -> useCase.execute(GoodsIssueReferenceType.PURCHASE_RETURN, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.reference.required");
    }

    @Test
    void execute_withoutReference_returnsBlankManualDraft() {
        GetGoodsIssueCreateViewUseCase useCase = new GetGoodsIssueCreateViewUseCaseImpl(
                new GoodsIssueSourceResolverRegistry(List.of())
        );

        GoodsIssue result = useCase.execute(null, null);

        assertThat(result.getReferenceType()).isEqualTo(GoodsIssueReferenceType.MANUAL);
        assertThat(result.getReferenceId()).isNull();
        assertThat(result.getLines()).isEmpty();
    }

    private static GoodsIssueSourceResolver resolver(GoodsIssueReferenceType referenceType, GoodsIssue draft) {
        return new GoodsIssueSourceResolver() {
            @Override
            public GoodsIssueReferenceType getReferenceType() {
                return referenceType;
            }

            @Override
            public GoodsIssue resolve(Long referenceId) {
                return draft;
            }
        };
    }
}
