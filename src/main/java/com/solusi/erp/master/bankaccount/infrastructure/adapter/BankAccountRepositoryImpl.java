package com.solusi.erp.master.bankaccount.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.util.PageableMapper;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.domain.repository.BankAccountRepository;
import com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccountPersistenceMapper;
import com.solusi.erp.master.model.Geographic;
import com.solusi.erp.master.model.Party;
import com.solusi.erp.master.geographic.infrastructure.persistence.GeographicJpaRepository;
import com.solusi.erp.master.party.infrastructure.persistence.PartyJpaRepository;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of BankAccountRepository.
 * Bridges Domain and Infrastructure Persistence.
 */
public class BankAccountRepositoryImpl implements BankAccountRepository {

    private final com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccountJpaRepository jpaRepository;
    private final BankAccountPersistenceMapper mapper;
    private final GeographicJpaRepository geographicRepository;
    private final PartyJpaRepository partyRepository;

    public BankAccountRepositoryImpl(
            com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccountJpaRepository jpaRepository,
            BankAccountPersistenceMapper mapper,
            GeographicJpaRepository geographicRepository,
            PartyJpaRepository partyRepository) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.geographicRepository = geographicRepository;
        this.partyRepository = partyRepository;
    }

    @Override
    public BankAccount save(BankAccount domain) {
        com.solusi.erp.master.model.BankAccount entity = mapper.toEntity(domain);

        Geographic city = geographicRepository.findById(domain.getCityId())
                .orElseThrow(() -> new com.solusi.erp.core.exception.DomainException("msg.error.bank-account.city-notfound"));
        entity.setCity(city);

        Party party = partyRepository.findById(domain.getPartyId())
                .orElseThrow(() -> new com.solusi.erp.core.exception.DomainException("msg.error.bank-account.party-notfound"));
        entity.setParty(party);

        com.solusi.erp.master.model.BankAccount saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<BankAccount> findById(Long id) {
        return jpaRepository.findByIdAndIsActiveTrue(id).map(mapper::toDomain);
    }

    @Override
    public Page<BankAccount> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<com.solusi.erp.master.model.BankAccount> springPage =
                (keyword != null && !keyword.isBlank())
                        ? jpaRepository.search(keyword, springPageable)
                        : jpaRepository.findByIsActiveTrue(springPageable);
        return new Page<>(
                springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements()
        );
    }

    @Override
    public void delete(Long id) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setIsActive(false);
            jpaRepository.save(entity);
        });
    }
}


