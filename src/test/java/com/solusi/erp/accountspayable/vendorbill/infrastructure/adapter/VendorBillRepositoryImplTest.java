package com.solusi.erp.accountspayable.vendorbill.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillGrRef;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillLine;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillStatus;
import com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence.VendorBillEntity;
import com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence.VendorBillJpaRepository;
import com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence.VendorBillLineEntity;
import com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence.VendorBillPersistenceMapper;
import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendorBillRepositoryImplTest {

    @Mock
    private VendorBillJpaRepository jpaRepository;

    @Mock
    private VendorBillPersistenceMapper mapper;

    @InjectMocks
    private VendorBillRepositoryImpl repository;

    @Test
    void save_then_findById_should_roundtrip_domain_aggregate() {
        VendorBill domain = sampleBill();
        VendorBillEntity entity = new VendorBillEntity();
        entity.setId(10L);
        entity.setLines(new ArrayList<>(List.of(new VendorBillLineEntity())));
        entity.setGrRefs(new ArrayList<>());

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(mapper.toLineEntityList(any(), any())).thenReturn(List.of(new VendorBillLineEntity()));
        when(mapper.toGrRefEntityList(any(), any())).thenReturn(List.of());
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);
        when(jpaRepository.findById(10L)).thenReturn(Optional.of(entity));

        VendorBill saved = repository.save(domain);
        Optional<VendorBill> found = repository.findById(10L);

        assertThat(saved.getCode()).isEqualTo("VB-202605-00001");
        assertThat(found).containsSame(domain);
        verify(jpaRepository).save(entity);
        verify(jpaRepository).findById(10L);
    }

    private VendorBill sampleBill() {
        return new VendorBill(
                new AuditMetadata(10L, 1L, LocalDateTime.now(), 1L, LocalDateTime.now(), 1L),
                "VB-202605-00001",
                22L,
                "INV-123",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                1L,
                VendorBillStatus.CONFIRMED,
                new BigDecimal("100.0000"),
                new BigDecimal("11.0000"),
                new BigDecimal("111.0000"),
                "note",
                List.of(new VendorBillGrRef(10L, 99L)),
                List.of(new VendorBillLine(
                        1L,
                        1001L,
                        501L,
                        "Product A",
                        "Desc",
                        new BigDecimal("2.0000"),
                        1L,
                        "PCS",
                        new BigDecimal("50.0000"),
                        new BigDecimal("100.0000"),
                        new BigDecimal("11.0000"),
                        new BigDecimal("111.0000")
                ))
        );
    }
}
