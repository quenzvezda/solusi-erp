package com.solusi.erp.master.service.impl;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.master.dto.PartyRoleTypeRequest;
import com.solusi.erp.master.dto.PartyRoleTypeResponse;
import com.solusi.erp.master.mapper.PartyRoleTypeMapper;
import com.solusi.erp.master.model.PartyRoleType;
import com.solusi.erp.master.repository.PartyRoleTypeRepository;
import com.solusi.erp.master.service.PartyRoleTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Implementation of PartyRoleTypeService.
 */
@Service
@RequiredArgsConstructor
public class PartyRoleTypeServiceImpl implements PartyRoleTypeService {

    private final PartyRoleTypeRepository repository;
    private final PartyRoleTypeMapper mapper;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public Page<PartyRoleTypeResponse> findAll(String keyword, Pageable pageable) {
        Page<PartyRoleType> page = StringUtils.hasText(keyword)
                ? repository.search(keyword, pageable)
                : repository.findAll(pageable);
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PartyRoleTypeResponse findById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PartyRoleTypeRequest getEditData(Long id) {
        return mapper.toRequest(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public FormViewDto<PartyRoleTypeRequest, Void, PartyRoleTypeResponse> getEditView(Long id) {
        PartyRoleType entity = findOrThrow(id);
        return FormViewDto.<PartyRoleTypeRequest, Void, PartyRoleTypeResponse>builder()
                .request(mapper.toRequest(entity))
                .audit(mapper.toResponse(entity))
                .build();
    }

    @Override
    @Transactional
    public PartyRoleTypeResponse create(PartyRoleTypeRequest request) {
        PartyRoleType entity = mapper.toEntity(request);
        entity.setCode(sequenceGeneratorService.generate("PARTY-ROLE-TYPE"));
        PartyRoleType saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PartyRoleTypeResponse update(Long id, PartyRoleTypeRequest request) {
        PartyRoleType entity = findOrThrow(id);
        mapper.updateEntity(request, entity);
        PartyRoleType saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findOrThrow(id);
        repository.deleteById(id);
    }

    private PartyRoleType findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.party-role-type.notfound")));
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
