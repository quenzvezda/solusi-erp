package com.solusi.erp.inventory.goodsissue.application.usecase.query;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.container.domain.port.ContainerLookupProvider;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssuePartyType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsIssueQueryUseCaseTest {

    @Mock
    private GoodsIssueRepository goodsIssueRepository;

    @Mock
    private InventoryMovementJpaRepository movementRepository;

    @Mock
    private ProductLookupProvider productLookupProvider;

    @Mock
    private UomLookupProvider uomLookupProvider;

    @Mock
    private ContainerLookupProvider containerLookupProvider;

    @Mock
    private FacilityLookupProvider facilityLookupProvider;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private GetGoodsIssueUseCase getUseCase;
    private GetGoodsIssueEditViewUseCase editViewUseCase;
    private FindGoodsIssuesUseCase findUseCase;
    private GetGoodsIssueCancelViewUseCase cancelViewUseCase;
    private GetGoodsIssueJournalLinksUseCase journalLinksUseCase;

    @BeforeEach
    void setUp() {
        getUseCase = new GetGoodsIssueUseCaseImpl(goodsIssueRepository);
        editViewUseCase = new GetGoodsIssueEditViewUseCaseImpl(goodsIssueRepository);
        findUseCase = new FindGoodsIssuesUseCaseImpl(goodsIssueRepository);
        cancelViewUseCase = new GetGoodsIssueCancelViewUseCaseImpl(
                goodsIssueRepository,
                movementRepository,
                productLookupProvider,
                uomLookupProvider,
                containerLookupProvider,
                facilityLookupProvider);
        journalLinksUseCase = new GetGoodsIssueJournalLinksUseCaseImpl(journalEntryRepository);
    }

    @Test
    void getUseCase_delegatesToRepository() {
        GoodsIssue issue = org.mockito.Mockito.mock(GoodsIssue.class);
        when(goodsIssueRepository.findById(7L)).thenReturn(Optional.of(issue));

        Optional<GoodsIssue> result = getUseCase.execute(7L);

        assertThat(result).contains(issue);
        verify(goodsIssueRepository).findById(7L);
    }

    @Test
    void editViewUseCase_delegatesToRepository() {
        GoodsIssue issue = org.mockito.Mockito.mock(GoodsIssue.class);
        when(goodsIssueRepository.findById(7L)).thenReturn(Optional.of(issue));

        Optional<GoodsIssue> result = editViewUseCase.execute(7L);

        assertThat(result).contains(issue);
        verify(goodsIssueRepository).findById(7L);
    }

    @Test
    void findUseCase_delegatesToRepository() {
        Pageable pageable = Pageable.of(0, 10, "issueDate", "desc");
        Page<GoodsIssue> expected = new Page<>(List.of(), 0, 10, 0);
        when(goodsIssueRepository.findAll("PRTN-0007", GoodsIssueReferenceType.PURCHASE_RETURN, 7L, pageable))
                .thenReturn(expected);

        Page<GoodsIssue> result = findUseCase.execute(
                "PRTN-0007", GoodsIssueReferenceType.PURCHASE_RETURN, 7L, pageable);

        assertThat(result).isSameAs(expected);
        verify(goodsIssueRepository).findAll("PRTN-0007", GoodsIssueReferenceType.PURCHASE_RETURN, 7L, pageable);
    }

    @Test
    void cancelViewUseCase_buildsMovementKeyedLinesAndDefaultsTargetContainers() {
        GoodsIssue issue = issue(GoodsIssueReferenceType.MANUAL);
        InventoryMovementEntity movement = movement(700L, 5L);
        when(goodsIssueRepository.findById(7L)).thenReturn(Optional.of(issue));
        when(movementRepository.findByReferenceTypeAndReferenceIdOrderByIdAsc(ReferenceType.GOODS_ISSUE, 7L))
                .thenReturn(List.of(movement));
        when(productLookupProvider.resolve(201L)).thenReturn(new LookupDto(201L, "Product", "SKU-001"));
        when(containerLookupProvider.resolve(5L)).thenReturn(new LookupDto(5L, "Bin 01", "BIN-01"));
        when(facilityLookupProvider.resolve(3L)).thenReturn(new LookupDto(3L, "Main Warehouse", "WH-01"));

        GoodsIssueCancelView view = cancelViewUseCase.execute(7L);

        assertThat(view.directCancelAllowed()).isTrue();
        assertThat(view.facilityName()).isEqualTo("Main Warehouse");
        assertThat(view.lines()).singleElement().satisfies(line -> {
            assertThat(line.originalMovementId()).isEqualTo(700L);
            assertThat(line.productName()).isEqualTo("Product");
            assertThat(line.productCode()).isEqualTo("SKU-001");
            assertThat(line.quantityIssued()).isEqualByComparingTo("2.0000");
            assertThat(line.historicalContainerId()).isEqualTo(5L);
            assertThat(line.historicalContainerName()).isEqualTo("Bin 01");
            assertThat(line.historicalContainerCode()).isEqualTo("BIN-01");
        });
    }

    @Test
    void cancelViewUseCase_rejectsSourceOwnedGoodsIssue() {
        when(goodsIssueRepository.findById(7L)).thenReturn(Optional.of(issue(GoodsIssueReferenceType.PURCHASE_RETURN)));

        assertThatThrownBy(() -> cancelViewUseCase.execute(7L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.cancel.source.owned");
    }

    @Test
    void cancelViewUseCase_rejectsDraftAndMissingGoodsIssue() {
        when(goodsIssueRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cancelViewUseCase.execute(7L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.notfound");

        GoodsIssue draft = new GoodsIssue(
                new com.solusi.erp.core.domain.model.AuditMetadata(8L, 1L, null, null, null, null),
                "GI-202606-00002",
                LocalDate.of(2026, 6, 1),
                GoodsIssueReferenceType.MANUAL,
                null,
                null,
                11L,
                GoodsIssuePartyType.SUPPLIER,
                3L,
                1L,
                BigDecimal.ONE,
                GoodsIssueStatus.DRAFT,
                null,
                List.of()
        );
        when(goodsIssueRepository.findById(8L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> cancelViewUseCase.execute(8L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.cancel.only.completed");
    }

    @Test
    void cancelViewUseCase_rejectsWhenNoOutboundMovementsExist() {
        GoodsIssue issue = issue(GoodsIssueReferenceType.MANUAL);
        InventoryMovementEntity receipt = movement(700L, 5L, MovementType.RECEIPT, new BigDecimal("2.0000"));
        InventoryMovementEntity positiveAdjustment = movement(701L, 5L, MovementType.ADJUSTMENT, new BigDecimal("1.0000"));
        when(goodsIssueRepository.findById(7L)).thenReturn(Optional.of(issue));
        when(movementRepository.findByReferenceTypeAndReferenceIdOrderByIdAsc(ReferenceType.GOODS_ISSUE, 7L))
                .thenReturn(List.of(receipt, positiveAdjustment));

        assertThatThrownBy(() -> cancelViewUseCase.execute(7L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.cancel.movements.notfound");
    }

    @Test
    void cancelViewUseCase_handlesMissingLookupLabelsAndOutboundMovementTypes() {
        GoodsIssue issue = issue(GoodsIssueReferenceType.MANUAL);
        InventoryMovementEntity reservedIssue = movement(700L, 5L, MovementType.ISSUE_RESERVED, new BigDecimal("2.0000"));
        InventoryMovementEntity transferOut = movement(701L, 6L, MovementType.TRANSFER_OUT, new BigDecimal("3.0000"));
        InventoryMovementEntity negativeAdjustment = movement(702L, 7L, MovementType.ADJUSTMENT, new BigDecimal("-1.0000"));
        when(goodsIssueRepository.findById(7L)).thenReturn(Optional.of(issue));
        when(movementRepository.findByReferenceTypeAndReferenceIdOrderByIdAsc(ReferenceType.GOODS_ISSUE, 7L))
                .thenReturn(List.of(reservedIssue, transferOut, negativeAdjustment));

        GoodsIssueCancelView view = cancelViewUseCase.execute(7L);

        assertThat(view.facilityName()).isNull();
        assertThat(view.lines()).hasSize(3);
        assertThat(view.lines().getFirst().productName()).isNull();
        assertThat(view.lines().getFirst().productCode()).isNull();
        assertThat(view.lines().getFirst().historicalContainerName()).isNull();
        assertThat(view.lines().getFirst().historicalContainerCode()).isNull();
    }

    @Test
    void journalLinksUseCase_returnsOriginalAndReversalJournalIds() {
        JournalEntry original = mock(JournalEntry.class);
        JournalEntry reversal = mock(JournalEntry.class);
        when(original.getId()).thenReturn(900L);
        when(reversal.getId()).thenReturn(901L);
        when(journalEntryRepository.findBySource("GOODS_ISSUE", 7L)).thenReturn(Optional.of(original));
        when(journalEntryRepository.findReversalOf(900L)).thenReturn(Optional.of(reversal));

        GoodsIssueJournalLinks links = journalLinksUseCase.execute(7L);

        assertThat(links.originalJournalId()).isEqualTo(900L);
        assertThat(links.reversalJournalId()).isEqualTo(901L);
        assertThat(links.hasOriginalJournal()).isTrue();
        assertThat(links.hasReversalJournal()).isTrue();
    }

    @Test
    void journalLinksUseCase_returnsEmptyLinksWhenOriginalOrReversalMissing() {
        when(journalEntryRepository.findBySource("GOODS_ISSUE", 7L)).thenReturn(Optional.empty());

        GoodsIssueJournalLinks missingOriginal = journalLinksUseCase.execute(7L);

        assertThat(missingOriginal.originalJournalId()).isNull();
        assertThat(missingOriginal.reversalJournalId()).isNull();
        assertThat(missingOriginal.hasOriginalJournal()).isFalse();
        assertThat(missingOriginal.hasReversalJournal()).isFalse();

        JournalEntry original = mock(JournalEntry.class);
        when(original.getId()).thenReturn(900L);
        when(journalEntryRepository.findBySource("GOODS_ISSUE", 8L)).thenReturn(Optional.of(original));
        when(journalEntryRepository.findReversalOf(900L)).thenReturn(Optional.empty());

        GoodsIssueJournalLinks missingReversal = journalLinksUseCase.execute(8L);

        assertThat(missingReversal.originalJournalId()).isEqualTo(900L);
        assertThat(missingReversal.reversalJournalId()).isNull();
        assertThat(missingReversal.hasOriginalJournal()).isTrue();
        assertThat(missingReversal.hasReversalJournal()).isFalse();
    }

    private static GoodsIssue issue(GoodsIssueReferenceType referenceType) {
        return new GoodsIssue(
                new com.solusi.erp.core.domain.model.AuditMetadata(7L, 1L, null, null, null, null),
                "GI-202606-00001",
                LocalDate.of(2026, 6, 1),
                referenceType,
                null,
                null,
                11L,
                GoodsIssuePartyType.SUPPLIER,
                3L,
                1L,
                BigDecimal.ONE,
                GoodsIssueStatus.COMPLETED,
                null,
                List.of()
        );
    }

    private static InventoryMovementEntity movement(Long id, Long containerId) {
        return movement(id, containerId, MovementType.ISSUE, new BigDecimal("2.0000"));
    }

    private static InventoryMovementEntity movement(Long id, Long containerId, MovementType movementType, BigDecimal quantity) {
        InventoryMovementEntity entity = new InventoryMovementEntity();
        entity.setId(id);
        entity.setProductId(201L);
        entity.setContainerId(containerId);
        entity.setQuantity(quantity);
        entity.setMovementType(movementType);
        entity.setReferenceType(ReferenceType.GOODS_ISSUE);
        entity.setReferenceId(7L);
        entity.setReferenceCode("GI-202606-00001");
        return entity;
    }
}
