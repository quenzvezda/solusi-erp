package com.solusi.erp.accounting.journal.infrastructure.adapter;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalEntryFilter;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalEntryJpaRepository;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalPersistenceMapper;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalEntryQueryPortImplTest {

    @Mock private JournalEntryJpaRepository jpaRepository;
    @Mock private JournalPersistenceMapper mapper;
    @Mock private Root root;
    @Mock private CriteriaQuery query;
    @Mock private CriteriaBuilder cb;
    @Mock private Path path;

    @Test
    void findJournalEntries_callsRepositoryWithSpec() {
        JournalEntryQueryPortImpl port = new JournalEntryQueryPortImpl(jpaRepository, mapper);
        when(jpaRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        JournalEntryFilter filter = new JournalEntryFilter(
                SchemaEventType.GOODS_RECEIPT, "GR-123", "JNL-001",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)
        );

        Page<JournalEntry> result = port.findJournalEntries(filter, new Pageable(1, 10, null, null));
        assertThat(result).isNotNull();

        ArgumentCaptor<Specification> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(jpaRepository).findAll(specCaptor.capture(), any(org.springframework.data.domain.Pageable.class));

        Specification spec = specCaptor.getValue();
        
        when(root.get(anyString())).thenReturn(path);
        when(cb.lower(any())).thenReturn(path);
        
        spec.toPredicate(root, query, cb);
        
        verify(cb).equal(path, "GOODS_RECEIPT");
        verify(cb).like(path, "%gr-123%");
        verify(cb).equal(path, 1L); // JNL-001 parses to 1L
        verify(cb).greaterThanOrEqualTo(path, LocalDate.of(2026, 1, 1));
        verify(cb).lessThanOrEqualTo(path, LocalDate.of(2026, 12, 31));
    }

    @Test
    void findJournalEntries_withInvalidJournalCode_handlesParseError() {
        JournalEntryQueryPortImpl port = new JournalEntryQueryPortImpl(jpaRepository, mapper);
        when(jpaRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        JournalEntryFilter filter = new JournalEntryFilter(
                null, null, "INVALID", null, null
        );

        port.findJournalEntries(filter, new Pageable(1, 10, null, null));

        ArgumentCaptor<Specification> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(jpaRepository).findAll(specCaptor.capture(), any(org.springframework.data.domain.Pageable.class));

        Specification spec = specCaptor.getValue();
        when(root.get(anyString())).thenReturn(path);
        
        spec.toPredicate(root, query, cb);
        
        verify(cb).equal(path, -1L); // INVALID parses to -1L
    }

    @Test
    void getDetail_callsRepository() {
        JournalEntryQueryPortImpl port = new JournalEntryQueryPortImpl(jpaRepository, mapper);
        when(jpaRepository.findById(1L)).thenReturn(Optional.empty());
        port.getJournalEntryDetail(1L);
        verify(jpaRepository).findById(1L);
    }

    @Test
    void readMethods_areTransactionalReadOnly() throws NoSuchMethodException {
        Transactional findAllTx = JournalEntryQueryPortImpl.class
                .getMethod("findJournalEntries", JournalEntryFilter.class, Pageable.class)
                .getAnnotation(Transactional.class);
        Transactional detailTx = JournalEntryQueryPortImpl.class
                .getMethod("getJournalEntryDetail", Long.class)
                .getAnnotation(Transactional.class);

        assertThat(findAllTx).isNotNull();
        assertThat(findAllTx.readOnly()).isTrue();
        assertThat(detailTx).isNotNull();
        assertThat(detailTx.readOnly()).isTrue();
    }
}
