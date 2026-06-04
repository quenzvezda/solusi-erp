package com.solusi.erp.accountspayable.debitmemo.infrastructure.adapter;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoLine;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence.DebitMemoEntity;
import com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence.DebitMemoJpaRepository;
import com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence.DebitMemoLineEntity;
import com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence.DebitMemoPersistenceMapper;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebitMemoRepositoryImplTest {

    @Mock
    private DebitMemoJpaRepository jpaRepository;

    @Mock
    private DebitMemoPersistenceMapper mapper;

    @InjectMocks
    private DebitMemoRepositoryImpl repository;

    @Test
    void save_then_findById_should_roundtrip_domain_aggregate() {
        DebitMemo domain = sampleDebitMemo();
        DebitMemoEntity entity = new DebitMemoEntity();
        entity.setId(10L);
        entity.setLines(new ArrayList<>());

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(mapper.toLineEntityList(eq(domain.getLines()), eq(entity))).thenReturn(List.of(new DebitMemoLineEntity()));
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);
        when(jpaRepository.findById(10L)).thenReturn(Optional.of(entity));

        DebitMemo saved = repository.save(domain);
        Optional<DebitMemo> found = repository.findById(10L);

        assertThat(saved.getCode()).isEqualTo("DM-202606-00001");
        assertThat(found).containsSame(domain);
        verify(jpaRepository).save(entity);
        verify(jpaRepository).findById(10L);
    }

    @Test
    void findAll_should_pass_filters_to_jpa() {
        DebitMemoEntity entity = new DebitMemoEntity();
        DebitMemo domain = sampleDebitMemo();
        Pageable pageable = Pageable.of(0, 20, "code", "asc");
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        when(jpaRepository.findAllFiltered(
                eq("PRT"),
                eq(22L),
                eq(DebitMemoSettlementStatus.OPEN),
                eq(from),
                eq(to),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Page<DebitMemo> result = repository.findAll(
                " PRT ",
                22L,
                DebitMemoSettlementStatus.OPEN,
                from,
                to,
                pageable
        );

        assertThat(result.content()).containsExactly(domain);
        assertThat(result.totalElements()).isEqualTo(1);
        verify(jpaRepository).findAllFiltered(
                eq("PRT"),
                eq(22L),
                eq(DebitMemoSettlementStatus.OPEN),
                eq(from),
                eq(to),
                any(org.springframework.data.domain.Pageable.class)
        );
    }

    @Test
    void findByPurchaseReturnId_should_map_entity_to_domain() {
        DebitMemoEntity entity = new DebitMemoEntity();
        DebitMemo domain = sampleDebitMemo();

        when(jpaRepository.findByPurchaseReturnId(100L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<DebitMemo> result = repository.findByPurchaseReturnId(100L);

        assertThat(result).containsSame(domain);
    }

    @Test
    void metadata_uniqueness_checks_should_delegate_to_jpa() {
        when(jpaRepository.existsSupplierMemoNumber(22L, "SUP-DM-001", 10L)).thenReturn(true);
        when(jpaRepository.existsTaxDocumentNumber("TAX-001", 10L)).thenReturn(true);

        assertThat(repository.existsSupplierMemoNumber(22L, "SUP-DM-001", 10L)).isTrue();
        assertThat(repository.existsTaxDocumentNumber("TAX-001", 10L)).isTrue();
    }

    private DebitMemo sampleDebitMemo() {
        return DebitMemo.reconstitute(
                new AuditMetadata(10L, 1L, LocalDateTime.now(), 1L, LocalDateTime.now(), 1L),
                "DM-202606-00001",
                100L,
                "PRT-202606-00001",
                22L,
                1L,
                LocalDate.of(2026, 6, 2),
                DebitMemoSettlementStatus.OPEN,
                null,
                null,
                null,
                null,
                "note",
                List.of(new DebitMemoLine(
                        1L,
                        1001L,
                        501L,
                        new BigDecimal("2.0000"),
                        1L,
                        new BigDecimal("100.0000"),
                        new BigDecimal("11.0000"),
                        new BigDecimal("100.0000"),
                        new BigDecimal("11.0000")
                ))
        );
    }
}

