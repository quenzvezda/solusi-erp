package com.solusi.erp.master.service.impl;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.master.dto.BankAccountRequestDto;
import com.solusi.erp.master.dto.BankAccountResponseDto;
import com.solusi.erp.master.mapper.BankAccountMapper;
import com.solusi.erp.master.model.BankAccount;
import com.solusi.erp.master.model.Geographic;
import com.solusi.erp.master.model.Party;
import com.solusi.erp.master.repository.BankAccountRepository;
import com.solusi.erp.master.repository.GeographicRepository;
import com.solusi.erp.master.repository.PartyRepository;
import com.solusi.erp.master.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository repository;
    private final BankAccountMapper mapper;
    private final GeographicRepository geographicRepository;
    private final PartyRepository partyRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public Page<BankAccountResponseDto> findAll(String keyword, Pageable pageable) {
        Page<BankAccount> page;
        if (StringUtils.hasText(keyword)) {
            page = repository.search(keyword, pageable);
        } else {
            page = repository.findByIsActiveTrue(pageable);
        }
        return page.map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public BankAccountResponseDto findById(Long id) {
        BankAccount entity = repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException(getMessage("master.bank-account.not-found")));
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public BankAccountRequestDto getEditData(Long id) {
        BankAccount entity = repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException(getMessage("master.bank-account.not-found")));
        
        BankAccountRequestDto dto = new BankAccountRequestDto();
        dto.setCode(entity.getCode());
        dto.setBankName(entity.getBankName());
        dto.setBranch(entity.getBranch());
        dto.setCityId(entity.getCity().getId());
        dto.setPartyId(entity.getParty().getId());
        dto.setAccountName(entity.getAccountName());
        dto.setAccountNo(entity.getAccountNo());
        dto.setAccountType(entity.getAccountType());
        dto.setNote(entity.getNote());
        
        return dto;
    }

    @Override
    @Transactional
    public void create(BankAccountRequestDto request) {
        BankAccount entity = mapper.toEntity(request);
        
        Geographic city = geographicRepository.findById(request.getCityId())
                .orElseThrow(() -> new RuntimeException(getMessage("master.bank-account.city-not-found")));
        entity.setCity(city);
        
        Party party = partyRepository.findById(request.getPartyId())
                .orElseThrow(() -> new RuntimeException(getMessage("master.bank-account.party-not-found")));
        entity.setParty(party);
        
        // Auto-generate code
        String generatedCode = sequenceGeneratorService.generate("BANK_ACCOUNT");
        entity.setCode(generatedCode);
        
        repository.save(entity);
    }

    @Override
    @Transactional
    public void update(Long id, BankAccountRequestDto request) {
        BankAccount entity = repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException(getMessage("master.bank-account.not-found")));

        mapper.updateEntityFromDto(request, entity);
        
        if (!entity.getCity().getId().equals(request.getCityId())) {
            Geographic city = geographicRepository.findById(request.getCityId())
                    .orElseThrow(() -> new RuntimeException(getMessage("master.bank-account.city-not-found")));
            entity.setCity(city);
        }
        
        if (!entity.getParty().getId().equals(request.getPartyId())) {
            Party party = partyRepository.findById(request.getPartyId())
                    .orElseThrow(() -> new RuntimeException(getMessage("master.bank-account.party-not-found")));
            entity.setParty(party);
        }

        repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        BankAccount entity = repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException(getMessage("master.bank-account.not-found")));
        
        entity.setIsActive(false);
        repository.save(entity);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
