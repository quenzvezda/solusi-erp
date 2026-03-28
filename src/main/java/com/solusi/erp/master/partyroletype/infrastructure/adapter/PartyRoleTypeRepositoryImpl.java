package com.solusi.erp.master.partyroletype.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.util.PageableMapper;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;
import com.solusi.erp.master.partyroletype.domain.repository.PartyRoleTypeRepository;
import com.solusi.erp.master.partyroletype.infrastructure.persistence.PartyRoleTypePersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of PartyRoleTypeRepository.
 * Bridges Domain and Infrastructure Persistence.
 */
public class PartyRoleTypeRepositoryImpl implements PartyRoleTypeRepository {

    private final com.solusi.erp.master.repository.PartyRoleTypeRepository jpaRepository;
    private final PartyRoleTypePersistenceMapper mapper;

    public PartyRoleTypeRepositoryImpl(
            com.solusi.erp.master.repository.PartyRoleTypeRepository jpaRepository,
            PartyRoleTypePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public PartyRoleType save(PartyRoleType domain) {
        com.solusi.erp.master.model.PartyRoleType entity = mapper.toEntity(domain);
        com.solusi.erp.master.model.PartyRoleType saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PartyRoleType> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<PartyRoleType> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<com.solusi.erp.master.model.PartyRoleType> springPage =
                (keyword != null && !keyword.isBlank())
                        ? jpaRepository.search(keyword, springPageable)
                        : jpaRepository.findAll(springPageable);
        return new Page<>(
                springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements()
        );
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }
}
