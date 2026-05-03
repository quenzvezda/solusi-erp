package com.solusi.erp.accounting.journal.infrastructure.adapter;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalEntryFilter;
import com.solusi.erp.accounting.journal.domain.port.JournalEntryQueryPort;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalEntryEntity;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalEntryJpaRepository;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalPersistenceMapper;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JournalEntryQueryPortImpl implements JournalEntryQueryPort {
    private final JournalEntryJpaRepository jpaRepository;
    private final JournalPersistenceMapper mapper;

    public JournalEntryQueryPortImpl(JournalEntryJpaRepository jpaRepository, JournalPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<JournalEntry> findJournalEntries(JournalEntryFilter filter, Pageable pageable) {
        Specification<JournalEntryEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.sourceType() != null) {
                predicates.add(cb.equal(root.get("eventType"), filter.sourceType().name()));
            }
            if (StringUtils.hasText(filter.sourceCode())) {
                predicates.add(cb.like(cb.lower(root.get("sourceCode")), "%" + filter.sourceCode().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filter.journalCode())) {
                predicates.add(cb.equal(root.get("id"), tryParseId(filter.journalCode())));
            }
            if (filter.postingDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("postingDate"), filter.postingDateFrom()));
            }
            if (filter.postingDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("postingDate"), filter.postingDateTo()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        org.springframework.data.domain.Page<JournalEntryEntity> springPage = 
                jpaRepository.findAll(spec, PageableMapper.toSpring(pageable));
        
        List<JournalEntry> content = springPage.getContent().stream().map(mapper::toDomain).toList();
        return new Page<>(content, pageable.page(), pageable.size(), springPage.getTotalElements());
    }

    @Override
    public Optional<JournalEntry> getJournalEntryDetail(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
    
    private Long tryParseId(String code) {
        try {
            return Long.parseLong(code.replace("JNL-", "").replace("JNL", ""));
        } catch (Exception e) {
            return -1L;
        }
    }
}