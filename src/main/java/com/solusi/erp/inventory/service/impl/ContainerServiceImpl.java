package com.solusi.erp.inventory.service.impl;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.dto.ContainerRequest;
import com.solusi.erp.inventory.dto.ContainerResponse;
import com.solusi.erp.inventory.mapper.WarehouseMapper;
import com.solusi.erp.inventory.model.Container;
import com.solusi.erp.inventory.repository.ContainerRepository;
import com.solusi.erp.inventory.service.ContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Implementation of ContainerService.
 */
@Service
@RequiredArgsConstructor
public class ContainerServiceImpl implements ContainerService {

    private final ContainerRepository repository;
    private final WarehouseMapper mapper;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public List<ContainerResponse> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContainerResponse> findAll(String keyword, Long gridId, Pageable pageable) {
        Page<Container> page;
        if (StringUtils.hasText(keyword)) {
            page = repository.search(keyword, pageable);
        } else if (gridId != null) {
            page = repository.findByGridId(gridId, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ContainerResponse findById(Long id) {
        Container entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.container.notfound")));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ContainerRequest getEditData(Long id) {
        Container entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.container.notfound")));
        return mapper.toRequest(entity);
    }

    @Override
    @Transactional
    public void create(ContainerRequest request) {
        if (StringUtils.hasText(request.getBarcode()) && repository.existsByBarcode(request.getBarcode())) {
            throw new RuntimeException(getMessage("msg.error.container.duplicate-barcode"));
        }
        
        Container entity = mapper.toEntity(request);
        String code = sequenceGeneratorService.generate("CONTAINER");
        entity.setCode(code);
        
        repository.save(entity);
    }

    @Override
    @Transactional
    public void update(Long id, ContainerRequest request) {
        Container entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.container.notfound")));

        if (StringUtils.hasText(request.getBarcode()) && repository.existsByBarcodeAndIdNot(request.getBarcode(), id)) {
            throw new RuntimeException(getMessage("msg.error.container.duplicate-barcode"));
        }

        mapper.updateEntityFromRequest(request, entity);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException(getMessage("msg.error.container.notfound"));
        }
        repository.deleteById(id);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
