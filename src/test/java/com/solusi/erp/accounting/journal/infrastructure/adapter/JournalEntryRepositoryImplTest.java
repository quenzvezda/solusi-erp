package com.solusi.erp.accounting.journal.infrastructure.adapter;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.model.JournalStatus;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalEntryEntity;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalEntryJpaRepository;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalPersistenceMapper;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    void save_mapsAndPersists() {
        JournalEntry domain = sampleEntry();
        JournalEntryEntity entity = new JournalEntryEntity();
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        JournalEntry result = repository.save(domain);

        assertThat(result).isEqualTo(domain);
        verify(jpaRepository).save(entity);
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
}
