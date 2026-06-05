package com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.adapter;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationLine;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationHistory;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationEntity;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationJpaRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationLineEntity;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationPersistenceMapper;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebitMemoAllocationRepositoryImplTest {

    @Mock
    private DebitMemoAllocationJpaRepository jpaRepository;

    @Mock
    private DebitMemoAllocationPersistenceMapper mapper;

    @InjectMocks
    private DebitMemoAllocationRepositoryImpl repository;

    @Test
    void save_then_findById_should_roundtrip_domain_aggregate() {
        DebitMemoAllocation domain = sampleAllocation();
        DebitMemoAllocationEntity entity = new DebitMemoAllocationEntity();
        entity.setId(10L);
        entity.setLines(new ArrayList<>());
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(mapper.toLineEntityList(eq(domain.getLines()), eq(entity))).thenReturn(List.of(new DebitMemoAllocationLineEntity()));
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);
        when(jpaRepository.findById(10L)).thenReturn(Optional.of(entity));

        DebitMemoAllocation saved = repository.save(domain);
        Optional<DebitMemoAllocation> found = repository.findById(10L);

        assertThat(saved.getCode()).isEqualTo("DMA-202606-00001");
        assertThat(found).containsSame(domain);
        verify(jpaRepository).save(entity);
        verify(jpaRepository).findById(10L);
    }

    @Test
    void findAll_should_pass_filters_to_jpa() {
        DebitMemoAllocationEntity entity = new DebitMemoAllocationEntity();
        DebitMemoAllocation domain = sampleAllocation();
        Pageable pageable = Pageable.of(0, 20, "code", "asc");
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        when(jpaRepository.findAllFiltered(
                eq("DM"),
                eq(100L),
                eq(DebitMemoAllocationStatus.DRAFT),
                eq(from),
                eq(to),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Page<DebitMemoAllocation> result = repository.findAll(" DM ", 100L, DebitMemoAllocationStatus.DRAFT, from, to, pageable);

        assertThat(result.content()).containsExactly(domain);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void consumption_and_sum_queries_should_delegate_to_jpa() {
        when(jpaRepository.existsActiveConsumptionByDebitMemoId(100L)).thenReturn(true);
        when(jpaRepository.sumConfirmedAppliedByDebitMemoId(100L)).thenReturn(new BigDecimal("75.0000"));
        DebitMemoAllocationJpaRepository.DebitMemoAllocationSumProjection projection =
                new DebitMemoAllocationJpaRepository.DebitMemoAllocationSumProjection() {
                    @Override
                    public Long getDebitMemoId() {
                        return 100L;
                    }

                    @Override
                    public BigDecimal getAppliedAmount() {
                        return new BigDecimal("75.0000");
                    }
                };
        when(jpaRepository.sumConfirmedAppliedByDebitMemoIds(List.of(100L))).thenReturn(List.of(projection));

        assertThat(repository.existsActiveConsumptionByDebitMemoId(100L)).isTrue();
        assertThat(repository.sumConfirmedAppliedByDebitMemoId(100L)).isEqualByComparingTo("75.0000");
        assertThat(repository.sumConfirmedAppliedByDebitMemoIds(List.of(100L))).isEqualTo(Map.of(100L, new BigDecimal("75.0000")));
        assertThat(repository.sumConfirmedAppliedByDebitMemoIds(List.of())).isEmpty();
    }

    @Test
    void history_queries_should_return_one_row_per_matching_line() {
        DebitMemoAllocationEntity entity = new DebitMemoAllocationEntity();
        DebitMemoAllocation domain = sampleAllocation();
        when(jpaRepository.findHistoryByDebitMemoId(100L)).thenReturn(List.of(entity));
        when(jpaRepository.findHistoryByVendorBillId(502L)).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        List<DebitMemoAllocationHistory> debitMemoHistory = repository.findHistoryByDebitMemoId(100L);
        List<DebitMemoAllocationHistory> vendorBillHistory = repository.findHistoryByVendorBillId(502L);

        assertThat(debitMemoHistory).hasSize(2);
        assertThat(vendorBillHistory).hasSize(1);
        assertThat(vendorBillHistory.getFirst().vendorBillId()).isEqualTo(502L);
        assertThat(vendorBillHistory.getFirst().appliedGrossOriginal()).isEqualByComparingTo("40.0000");
    }

    @Test
    void consumption_adapter_should_use_repository_active_consumption_query() {
        when(jpaRepository.existsActiveConsumptionByDebitMemoId(100L)).thenReturn(true);
        DebitMemoAllocationConsumptionAdapter adapter = new DebitMemoAllocationConsumptionAdapter(repository);

        assertThat(adapter.hasConfirmedConsumption(100L)).isTrue();
    }

    private DebitMemoAllocation sampleAllocation() {
        DebitMemoAllocation allocation = DebitMemoAllocation.reconstitute(
                new AuditMetadata(10L, 1L, LocalDateTime.now(), 1L, LocalDateTime.now(), 1L),
                "DMA-202606-00001",
                100L,
                "DM-202606-00001",
                LocalDate.of(2026, 6, 5),
                DebitMemoAllocationStatus.DRAFT,
                null,
                null,
                null,
                null,
                "notes",
                List.of(line(501L, "60.0000"), line(502L, "40.0000"))
        );
        return allocation;
    }

    private DebitMemoAllocationLine line(Long vendorBillId, String amount) {
        return new DebitMemoAllocationLine(
                null,
                vendorBillId,
                "VB-202606-" + vendorBillId,
                new BigDecimal("100.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal(amount),
                new BigDecimal(amount),
                BigDecimal.ZERO,
                new BigDecimal(amount),
                BigDecimal.ZERO,
                BigDecimal.ONE,
                new BigDecimal(amount),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }
}
