package com.solusi.erp.accounting.journal.infrastructure.adapter;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalEntryFilter;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalEntryJpaRepository;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalPersistenceMapper;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JournalEntryQueryPortImplTest {
    @Mock private JournalEntryJpaRepository jpaRepository;
    @Mock private JournalPersistenceMapper mapper;

    @Test
    void findJournalEntries_callsRepository() {
        JournalEntryQueryPortImpl port = new JournalEntryQueryPortImpl(jpaRepository, mapper);
        when(jpaRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        
        Page<JournalEntry> result = port.findJournalEntries(new JournalEntryFilter(null, null, null, null, null), new Pageable(1, 10, null, null));
        
        assertThat(result).isNotNull();
        verify(jpaRepository).findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void getDetail_callsRepository() {
        JournalEntryQueryPortImpl port = new JournalEntryQueryPortImpl(jpaRepository, mapper);
        when(jpaRepository.findById(1L)).thenReturn(Optional.empty());
        
        port.getJournalEntryDetail(1L);
        
        verify(jpaRepository).findById(1L);
    }
}