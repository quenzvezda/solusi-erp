package com.solusi.erp.master.service.impl;

import com.solusi.erp.master.dto.GeographicDto;
import com.solusi.erp.master.mapper.GeographicMapper;
import com.solusi.erp.master.model.Geographic;
import com.solusi.erp.master.model.GeographicType;
import com.solusi.erp.master.repository.GeographicRepository;
import com.solusi.erp.master.service.GeographicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeographicServiceImpl implements GeographicService {

    private final GeographicRepository geographicRepository;
    private final GeographicMapper geographicMapper;
    private final MessageSource messageSource;

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GeographicDto> getAllGeographics(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return geographicRepository.search(keyword, pageable).map(geographicMapper::toDto);
        }
        return geographicRepository.findAll(pageable).map(geographicMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GeographicDto> getByParent(Long parentId, Pageable pageable) {
        return geographicRepository.findByParentId(parentId, pageable).map(geographicMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public GeographicDto getById(Long id) {
        Geographic geographic = geographicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
        return geographicMapper.toDto(geographic);
    }

    @Override
    @Transactional
    public GeographicDto create(GeographicDto dto) {
        Geographic geographic = geographicMapper.toEntity(dto);
        if (dto.getParentId() != null) {
            Geographic parent = geographicRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
            geographic.setParent(parent);
        }
        geographic = geographicRepository.save(geographic);
        return geographicMapper.toDto(geographic);
    }

    @Override
    @Transactional
    public GeographicDto update(Long id, GeographicDto dto) {
        Geographic geographic = geographicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
        
        geographicMapper.updateEntity(dto, geographic);
        
        if (dto.getParentId() != null) {
            Geographic parent = geographicRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
            geographic.setParent(parent);
        } else {
            geographic.setParent(null);
        }
        
        geographic = geographicRepository.save(geographic);
        return geographicMapper.toDto(geographic);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Geographic geographic = geographicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
        geographic.setIsActive(false);
        geographicRepository.save(geographic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeographicDto> getByType(GeographicType type) {
        return geographicRepository.findByTypeAndIsActiveTrue(type).stream()
                .map(geographicMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeographicDto> getByParentActive(Long parentId) {
        return geographicRepository.findByParentIdAndIsActiveTrue(parentId).stream()
                .map(geographicMapper::toDto)
                .collect(Collectors.toList());
    }
}
