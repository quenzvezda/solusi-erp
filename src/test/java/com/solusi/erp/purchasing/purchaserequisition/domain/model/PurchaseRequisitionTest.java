package com.solusi.erp.purchasing.purchaserequisition.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PurchaseRequisition Domain Model Tests")
class PurchaseRequisitionTest {

    private PurchaseRequisitionLine createValidLine() {
        return new PurchaseRequisitionLine(
            AuditMetadata.empty(), null,
            1L, new BigDecimal("10.0000"), 1L,
            LocalDate.of(2026, 8, 1),
            new BigDecimal("50.0000"), null, "Line note"
        );
    }

    @Nested
    @DisplayName("createNew factory method")
    class CreateNew {

        @Test
        @DisplayName("creates draft PR with valid parameters")
        void createNew_withValidParams_createsDraftPR() {
            PurchaseRequisition pr = PurchaseRequisition.createNew(
                "PR-202607-00001",
                LocalDate.of(2026, 7, 14),
                1L, 2L, "IT",
                PurchaseRequisitionPriority.NORMAL,
                "Initial request",
                null,
                1L,
                new ArrayList<>()
            );

            assertThat(pr.getId()).isNull();
            assertThat(pr.getCode()).isEqualTo("PR-202607-00001");
            assertThat(pr.getRequestDate()).isEqualTo(LocalDate.of(2026, 7, 14));
            assertThat(pr.getRequesterId()).isEqualTo(1L);
            assertThat(pr.getFacilityId()).isEqualTo(2L);
            assertThat(pr.getDepartment()).isEqualTo("IT");
            assertThat(pr.getPriority()).isEqualTo(PurchaseRequisitionPriority.NORMAL);
            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.DRAFT);
            assertThat(pr.getNote()).isEqualTo("Initial request");
            assertThat(pr.isActive()).isTrue();
        }

        @Test
        @DisplayName("creates PR with null optional fields")
        void createNew_withNullOptionals_succeeds() {
            PurchaseRequisition pr = PurchaseRequisition.createNew(
                "PR-202607-00002",
                LocalDate.of(2026, 7, 14),
                1L, null, null,
                PurchaseRequisitionPriority.LOW,
                null,
                null,
                1L,
                new ArrayList<>()
            );

            assertThat(pr.getFacilityId()).isNull();
            assertThat(pr.getDepartment()).isNull();
            assertThat(pr.getNote()).isNull();
        }
    }

    @Nested
    @DisplayName("full constructor")
    class FullConstructor {

        @Test
        @DisplayName("preserves all fields including metadata and status")
        void constructor_preservesAllFields() {
            AuditMetadata metadata = new AuditMetadata(5L, 3L, null, null, null, null);
            List<PurchaseRequisitionLine> lines = List.of(createValidLine());

            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-202607-00001",
                LocalDate.of(2026, 7, 14),
                1L, 2L, "Finance",
                PurchaseRequisitionPriority.HIGH,
                PurchaseRequisitionStatus.SUBMITTED,
                "Urgent need",
                true,
                null,
                1L,
                lines
            );

            assertThat(pr.getId()).isEqualTo(5L);
            assertThat(pr.getMetadata()).isEqualTo(metadata);
            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.SUBMITTED);
            assertThat(pr.getLines()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("update method")
    class Update {

        @Test
        @DisplayName("updates mutable fields when status is DRAFT")
        void update_whenDraft_updatesMutableFields() {
            PurchaseRequisition pr = PurchaseRequisition.createNew(
                "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL, "Old note", null, 1L, new ArrayList<>()
            );

            PurchaseRequisitionLine newLine = createValidLine();
            pr.update(
                LocalDate.of(2026, 7, 20),
                3L, "Operations",
                PurchaseRequisitionPriority.HIGH,
                "Updated note",
                null,
                1L,
                List.of(newLine)
            );

            assertThat(pr.getRequestDate()).isEqualTo(LocalDate.of(2026, 7, 20));
            assertThat(pr.getFacilityId()).isEqualTo(3L);
            assertThat(pr.getDepartment()).isEqualTo("Operations");
            assertThat(pr.getPriority()).isEqualTo(PurchaseRequisitionPriority.HIGH);
            assertThat(pr.getNote()).isEqualTo("Updated note");
            assertThat(pr.getLines()).hasSize(1);
        }

        @Test
        @DisplayName("throws DomainException when status is not DRAFT")
        void update_whenNotDraft_throwsDomainException() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.SUBMITTED, "note", true, null, 1L, new ArrayList<>()
            );

            assertThatThrownBy(() -> pr.update(
                LocalDate.of(2026, 7, 20), 3L, "Ops",
                PurchaseRequisitionPriority.HIGH, "New", null, 1L, List.of()
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.update.not.draft");
        }
    }

    @Nested
    @DisplayName("submit method")
    class Submit {

        @Test
        @DisplayName("changes status from DRAFT to SUBMITTED with valid lines")
        void submit_fromDraft_changesStatusToSubmitted() {
            PurchaseRequisition pr = PurchaseRequisition.createNew(
                "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL, null,
                null, 1L,
                new ArrayList<>(List.of(createValidLine()))
            );

            pr.submit();

            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.SUBMITTED);
        }

        @Test
        @DisplayName("throws DomainException when no lines")
        void submit_withNoLines_throwsDomainException() {
            PurchaseRequisition pr = PurchaseRequisition.createNew(
                "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL, null, null, 1L, new ArrayList<>()
            );

            assertThatThrownBy(() -> pr.submit())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.submit.no.lines");
        }

        @Test
        @DisplayName("throws DomainException when status is not DRAFT")
        void submit_whenNotDraft_throwsDomainException() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.APPROVED, null, true, null, 1L,
                new ArrayList<>(List.of(createValidLine()))
            );

            assertThatThrownBy(() -> pr.submit())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.submit.invalid.status");
        }
    }

    @Nested
    @DisplayName("approve method")
    class Approve {

        @Test
        @DisplayName("changes status from SUBMITTED to APPROVED")
        void approve_fromSubmitted_changesStatusToApproved() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.SUBMITTED, null, true, null, 1L,
                new ArrayList<>(List.of(createValidLine()))
            );

            pr.approve();

            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.APPROVED);
        }
    }

    @Nested
    @DisplayName("reject method")
    class Reject {

        @Test
        @DisplayName("changes status from SUBMITTED to REJECTED")
        void reject_fromSubmitted_changesStatusToRejected() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.SUBMITTED, null, true, null, 1L,
                new ArrayList<>(List.of(createValidLine()))
            );

            pr.reject();

            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.REJECTED);
        }
    }

    @Nested
    @DisplayName("cancel method")
    class Cancel {

        @Test
        @DisplayName("cancels from DRAFT status")
        void cancel_fromDraft_changesStatusToCancelled() {
            PurchaseRequisition pr = PurchaseRequisition.createNew(
                "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL, null, null, 1L, new ArrayList<>()
            );

            pr.cancel();

            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.CANCELLED);
        }

        @Test
        @DisplayName("cancels from SUBMITTED status")
        void cancel_fromSubmitted_changesStatusToCancelled() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.SUBMITTED, null, true, null, 1L, new ArrayList<>()
            );

            pr.cancel();

            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.CANCELLED);
        }

        @Test
        @DisplayName("cancels from APPROVED status")
        void cancel_fromApproved_changesStatusToCancelled() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.APPROVED, null, true, null, 1L, new ArrayList<>()
            );

            pr.cancel();

            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.CANCELLED);
        }

        @Test
        @DisplayName("throws DomainException when status is CONVERTED")
        void cancel_fromConverted_throwsDomainException() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.CONVERTED, null, true, null, 1L, new ArrayList<>()
            );

            assertThatThrownBy(() -> pr.cancel())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.cancel.invalid.status");
        }

        @Test
        @DisplayName("throws DomainException when already CANCELLED")
        void cancel_fromCancelled_throwsDomainException() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.CANCELLED, null, true, null, 1L, new ArrayList<>()
            );

            assertThatThrownBy(() -> pr.cancel())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.cancel.invalid.status");
        }
    }

    @Nested
    @DisplayName("deactivate method")
    class Deactivate {

        @Test
        @DisplayName("deactivates from DRAFT status")
        void deactivate_fromDraft_setsActiveToFalse() {
            PurchaseRequisition pr = PurchaseRequisition.createNew(
                "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL, null, null, 1L, new ArrayList<>()
            );

            pr.deactivate();

            assertThat(pr.isActive()).isFalse();
        }

        @Test
        @DisplayName("throws DomainException when status is not DRAFT")
        void deactivate_whenNotDraft_throwsDomainException() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.SUBMITTED, null, true, null, 1L, new ArrayList<>()
            );

            assertThatThrownBy(() -> pr.deactivate())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.delete.not.draft");
        }
    }

    @Nested
    @DisplayName("PurchaseRequisitionLine validation")
    class LineValidation {

        @Test
        @DisplayName("line with positive quantity is valid")
        void line_withPositiveQuantity_isValid() {
            PurchaseRequisitionLine line = new PurchaseRequisitionLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("5.0000"), 1L,
                null, null, null, null
            );

            assertThat(line.getQuantity()).isEqualByComparingTo("5.0000");
        }

        @Test
        @DisplayName("line with zero quantity throws DomainException")
        void line_withZeroQuantity_throwsDomainException() {
            assertThatThrownBy(() -> new PurchaseRequisitionLine(
                AuditMetadata.empty(), null,
                1L, BigDecimal.ZERO, 1L,
                null, null, null, null
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.line.quantity.positive");
        }

        @Test
        @DisplayName("line with negative quantity throws DomainException")
        void line_withNegativeQuantity_throwsDomainException() {
            assertThatThrownBy(() -> new PurchaseRequisitionLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("-1.0000"), 1L,
                null, null, null, null
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.line.quantity.positive");
        }

        @Test
        @DisplayName("line with null quantity throws DomainException")
        void line_withNullQuantity_throwsDomainException() {
            assertThatThrownBy(() -> new PurchaseRequisitionLine(
                AuditMetadata.empty(), null,
                1L, null, 1L,
                null, null, null, null
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.line.quantity.positive");
        }
    }

    @Nested
    @DisplayName("getLines returns unmodifiable list")
    class GetLines {

        @Test
        @DisplayName("getLines returns unmodifiable view")
        void getLines_returnsUnmodifiableList() {
            PurchaseRequisition pr = PurchaseRequisition.createNew(
                "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL, null,
                null, 1L,
                new ArrayList<>(List.of(createValidLine()))
            );

            assertThatThrownBy(() -> pr.getLines().add(createValidLine()))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
