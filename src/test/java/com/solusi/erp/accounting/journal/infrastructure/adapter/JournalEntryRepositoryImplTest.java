package com.solusi.erp.accounting.journal.infrastructure.adapter;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.model.JournalStatus;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalEntryEntity;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalEntryJpaRepository;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalPersistenceMapper;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalEntryRepositoryImplTest {

    @Mock private JournalEntryJpaRepository jpaRepository;
    @Mock private JournalPersistenceMapper mapper;
    @InjectMocks private JournalEntryRepositoryImpl repository;

    @Test
    void existsBySource_delegatesToJpa() {
        when(jpaRepository.existsBySourceTypeAndSourceId("GOODS_RECEIPT", 6L)).thenReturn(true);

        assertThat(repository.existsBySource("GOODS_RECEIPT", 6L)).isTrue();
    }

    @Test
    void save_newDomain_mapsAndPersistsNewEntity() {
        JournalEntry domain = sampleEntry();
        JournalEntryEntity entity = new JournalEntryEntity();
        when(mapper.toNewEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        JournalEntry result = repository.save(domain);

        assertThat(result).isEqualTo(domain);
        verify(jpaRepository).save(entity);
        verify(jpaRepository, never()).findById(any());
    }

    @Test
    void save_existingDomain_loadsExistingAndAppliesChanges() {
        JournalEntry domain = sampleEntryWithId(10L);
        JournalEntryEntity existing = new JournalEntryEntity();
        JournalEntryEntity saved = new JournalEntryEntity();
        when(jpaRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(jpaRepository.save(existing)).thenReturn(saved);
        when(mapper.toDomain(saved)).thenReturn(domain);

        JournalEntry result = repository.save(domain);

        assertThat(result).isEqualTo(domain);
        verify(mapper).applyToEntity(domain, existing);
        verify(jpaRepository).save(existing);
    }

    @Test
    void save_existingDomainThrowsWhenEntityMissing() {
        JournalEntry domain = sampleEntryWithId(10L);
        when(jpaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repository.save(domain))
                .isInstanceOf(DomainException.class);

        verify(jpaRepository, never()).save(any());
    }

    @Test
    void findById_mapsJpaEntity() {
        JournalEntry domain = sampleEntryWithId(10L);
        JournalEntryEntity entity = new JournalEntryEntity();
        when(jpaRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        assertThat(repository.findById(10L)).contains(domain);
    }

    @Test
    void deleteById_delegatesToJpa() {
        repository.deleteById(10L);

        verify(jpaRepository).deleteById(10L);
    }

    @Test
    void existsReversalOf_delegatesToJpa() {
        when(jpaRepository.existsByReversalOfId(10L)).thenReturn(true);

        assertThat(repository.existsReversalOf(10L)).isTrue();
    }

    @Test
    void findReversalOf_delegatesAndMaps() {
        JournalEntry domain = sampleEntryWithId(11L);
        JournalEntryEntity entity = new JournalEntryEntity();
        when(jpaRepository.findByReversalOfId(10L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        assertThat(repository.findReversalOf(10L)).contains(domain);
    }

    private JournalEntry sampleEntry() {
        return JournalEntry.createPosted(
                SchemaEventType.GOODS_RECEIPT, "GOODS_RECEIPT", 6L, "GR-0006",
                LocalDate.now(), "Auto journal",
                List.of(
                        JournalLine.debit(101L, new BigDecimal("500.0000")),
                        JournalLine.credit(201L, new BigDecimal("500.0000"))
                )
        );
    }

    private JournalEntry sampleEntryWithId(Long id) {
        return new JournalEntry(
                new AuditMetadata(id, 1L, null, null, null, null),
                SchemaEventType.GOODS_RECEIPT.name(),
                "GOODS_RECEIPT",
                6L,
                "GR-0006",
                null,
                null,
                null,
                null,
                LocalDate.now(),
                "Auto journal",
                JournalStatus.POSTED,
                List.of(
                        JournalLine.debit(101L, new BigDecimal("500.0000")),
                        JournalLine.credit(201L, new BigDecimal("500.0000"))
                )
        );
    }
}
