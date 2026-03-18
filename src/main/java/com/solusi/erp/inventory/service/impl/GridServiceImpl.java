package com.solusi.erp.inventory.service.impl;

import com.solusi.erp.inventory.dto.GridRequest;
import com.solusi.erp.inventory.dto.GridResponse;
import com.solusi.erp.inventory.dto.InventoryLookupDto;
import com.solusi.erp.inventory.mapper.WarehouseMapper;
import com.solusi.erp.inventory.model.Grid;
import com.solusi.erp.inventory.repository.GridRepository;
import com.solusi.erp.inventory.service.GridService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Implementation of GridService.
 */
@Service
@RequiredArgsConstructor
public class GridServiceImpl implements GridService {

    private final GridRepository repository;
    private final WarehouseMapper mapper;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public InventoryLookupDto getLookupGrid(Long id) {
        Grid g = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.grid.notfound")));
        return InventoryLookupDto.builder()
                .id(g.getId())
                .name(g.getCode() + " - " + g.getName())
                .subText(g.getFacility().getName())
                .parentId(g.getFacility().getId())
                .parentName(g.getFacility().getCode() + " - " + g.getFacility().getName())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryLookupDto> lookupGrids(String keyword, Long facilityId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Grid> page;
        if (facilityId != null) {
            page = repository.searchByFacility(keyword, facilityId, pageable);
        } else {
            page = repository.search(keyword, pageable);
        }
        
        return page.getContent().stream()
                .map(g -> InventoryLookupDto.builder()
                        .id(g.getId())
                        .name(g.getCode() + " - " + g.getName())
                        .subText(g.getFacility().getName())
                        .parentId(g.getFacility().getId())
                        .parentName(g.getFacility().getCode() + " - " + g.getFacility().getName())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GridResponse> findAll(String keyword, Long facilityId, Pageable pageable) {
        Page<Grid> page;
        if (StringUtils.hasText(keyword)) {
            page = repository.search(keyword, pageable);
        } else if (facilityId != null) {
            page = repository.findByFacilityId(facilityId, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public GridResponse findById(Long id) {
        Grid entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.grid.notfound")));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public GridRequest getEditData(Long id) {
        Grid entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.grid.notfound")));
        return mapper.toRequest(entity);
    }

    @Override
    @Transactional
    public void create(GridRequest request) {
        if (repository.existsByFacilityIdAndCode(request.getFacilityId(), request.getCode())) {
            throw new RuntimeException(getMessage("msg.error.grid.duplicate-code"));
        }
        Grid entity = mapper.toEntity(request);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void update(Long id, GridRequest request) {
        Grid entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.grid.notfound")));

        if (repository.existsByFacilityIdAndCodeAndIdNot(request.getFacilityId(), request.getCode(), id)) {
            throw new RuntimeException(getMessage("msg.error.grid.duplicate-code"));
        }

        mapper.updateEntityFromRequest(request, entity);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException(getMessage("msg.error.grid.notfound"));
        }
        repository.deleteById(id);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
