package com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnLine;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReversalLine;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnEntity;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnJpaRepository;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnLineEntity;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnPersistenceMapper;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnReversalLineEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseReturnRepositoryImplTest {

    @Mock private PurchaseReturnJpaRepository jpaRepository;
    @Mock private PurchaseReturnPersistenceMapper mapper;
    @InjectMocks private PurchaseReturnRepositoryImpl repository;

    @Test
    void save_attachesLineAndReversalLineChildrenBeforePersisting() {
        PurchaseReturn domain = reversedDomain();
        PurchaseReturnEntity entity = new PurchaseReturnEntity();
        PurchaseReturnLineEntity lineEntity = new PurchaseReturnLineEntity();
        PurchaseReturnReversalLineEntity reversalLineEntity = new PurchaseReturnReversalLineEntity();

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(mapper.toLineEntityList(domain.getLines(), entity)).thenReturn(List.of(lineEntity));
        when(mapper.toReversalLineEntityList(domain.getReversalLines(), entity)).thenReturn(List.of(reversalLineEntity));
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        PurchaseReturn result = repository.save(domain);

        assertThat(result).isSameAs(domain);
        assertThat(entity.getLines()).containsExactly(lineEntity);
        assertThat(entity.getReversalLines()).containsExactly(reversalLineEntity);
        verify(jpaRepository).save(entity);
    }

    @Test
    void findByIdForUpdate_delegatesToLockedJpaQueryAndMapsResult() {
        PurchaseReturnEntity entity = new PurchaseReturnEntity();
        PurchaseReturn domain = reversedDomain();
        when(jpaRepository.findByIdWithLinesForUpdate(10L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        assertThat(repository.findByIdForUpdate(10L)).contains(domain);

        verify(jpaRepository).findByIdWithLinesForUpdate(10L);
    }

    private PurchaseReturn reversedDomain() {
        return PurchaseReturn.reconstitute(
                new AuditMetadata(10L, 0L, null, null, null, null),
                "PRT-001", LocalDate.of(2026, 6, 1), "GOODS_RECEIPT", 1L, "GR-001",
                2L, "PO-001", 3L, 4L, 5L, BigDecimal.ONE, PurchaseReturnStatus.REVERSED,
                PurchaseReturnReason.DAMAGED, null, 99L, 500L,
                LocalDate.of(2026, 6, 2), "Full reversal", 77L, 88L,
                List.of(line()), List.of(reversalLine()));
    }

    private PurchaseReturnLine line() {
        return PurchaseReturnLine.create(
                11L, 10L, false, BigDecimal.ONE, 20L, BigDecimal.ONE, 30L, 35L, 40L,
                null, PurchaseReturnReason.DAMAGED, null, "GOODS_RECEIPT", 1L, 11L,
                new BigDecimal("100"), new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private PurchaseReturnReversalLine reversalLine() {
        return PurchaseReturnReversalLine.create(11L, 900L, 40L, 10L, null, BigDecimal.ONE);
    }
}
