package com.solusi.erp.master.service.impl;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.dto.GeographicRequest;
import com.solusi.erp.master.dto.GeographicResponse;
import com.solusi.erp.master.form.GeographicUIForm;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public Page<GeographicResponse> getAllGeographics(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return geographicRepository.search(keyword, pageable).map(geographicMapper::toResponse);
        }
        return geographicRepository.findAll(pageable).map(geographicMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GeographicResponse> getByParent(Long parentId, Pageable pageable) {
        return geographicRepository.findByParentId(parentId, pageable).map(geographicMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public GeographicResponse getById(Long id) {
        Geographic geographic = geographicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
        return geographicMapper.toResponse(geographic);
    }

    @Override
    @Transactional(readOnly = true)
    public GeographicRequest getEditData(Long id) {
        Geographic geographic = geographicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
        return geographicMapper.toRequest(geographic);
    }

    @Override
    @Transactional(readOnly = true)
    public FormViewDto<GeographicRequest, GeographicUIForm, GeographicResponse> getGeographicEditView(Long id) {
        Geographic entity = geographicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));

        return FormViewDto.<GeographicRequest, GeographicUIForm, GeographicResponse>builder()
                .request(geographicMapper.toRequest(entity))
                .ui(geographicMapper.toUIForm(entity))
                .audit(geographicMapper.toResponse(entity))
                .build();
    }

    @Override
    @Transactional
    public GeographicResponse create(GeographicRequest request) {
        Geographic geographic = geographicMapper.toEntity(request);
        if (request.getParentId() != null) {
            Geographic parent = geographicRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
            geographic.setParent(parent);
        }
        Geographic saved = geographicRepository.save(geographic);
        return geographicMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public GeographicResponse update(Long id, GeographicRequest request) {
        Geographic geographic = geographicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));

        geographicMapper.updateEntity(request, geographic);

        if (request.getParentId() != null) {
            Geographic parent = geographicRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
            geographic.setParent(parent);
        } else {
            geographic.setParent(null);
        }

        Geographic saved = geographicRepository.save(geographic);
        return geographicMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Geographic geographic = geographicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
        geographic.setIsActive(false);
        geographicRepository.save(geographic);
    }

    // -----------------------------------------------------------------------
    // Lookup methods for TomSelect autocomplete
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<LookupDto> lookupCountries(String q, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("name").ascending());
        String keyword = q != null ? q.trim() : "";
        return geographicRepository.lookupByType(GeographicType.COUNTRY, keyword, pageable)
                .getContent()
                .stream()
                .map(g -> new LookupDto(g.getId(), g.getName(), g.getCode()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupDto> lookupProvinces(Long countryId, String q, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("name").ascending());
        String keyword = q != null ? q.trim() : "";
        Page<Geographic> pageResult = (countryId != null)
                ? geographicRepository.lookupProvincesByParent(countryId, keyword, pageable)
                : geographicRepository.lookupProvincesByType(GeographicType.STATE_PROVINCE, keyword, pageable);

        return pageResult.getContent()
                .stream()
                .map(g -> {
                    String countryName = g.getParent() != null ? g.getParent().getName() : "";
                    return new LookupDto(g.getId(), g.getName(), countryName);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupDto> lookupCities(Long provinceId, String q, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("name").ascending());
        String keyword = q != null ? q.trim() : "";
        Page<Geographic> pageResult = (provinceId != null)
                ? geographicRepository.lookupCitiesByParent(provinceId, keyword, pageable)
                : geographicRepository.lookupCitiesByType(GeographicType.CITY_MUNICIPALITY, keyword, pageable);

        return pageResult.getContent()
                .stream()
                .map(g -> {
                    String provinceName = g.getParent() != null ? g.getParent().getName() : "";
                    String countryName = (g.getParent() != null && g.getParent().getParent() != null)
                            ? g.getParent().getParent().getName()
                            : "";
                    String subText = provinceName.isEmpty() ? countryName
                            : (countryName.isEmpty() ? provinceName : provinceName + ", " + countryName);
                    return new LookupDto(g.getId(), g.getName(), subText);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LookupDto getLookupById(Long id) {
        Geographic g = geographicRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
        
        String subText;
        if (g.getType() == GeographicType.STATE_PROVINCE) {
            subText = g.getParent() != null ? g.getParent().getName() : g.getCode();
        } else if (g.getType() == GeographicType.CITY_MUNICIPALITY) {
            String provinceName = g.getParent() != null ? g.getParent().getName() : "";
            String countryName = (g.getParent() != null && g.getParent().getParent() != null)
                    ? g.getParent().getParent().getName()
                    : "";
            subText = provinceName.isEmpty() ? countryName
                    : (countryName.isEmpty() ? provinceName : provinceName + ", " + countryName);
        } else {
            subText = g.getCode();
        }
        
        return new LookupDto(g.getId(), g.getName(), subText);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeographicResponse> findByType(GeographicType type) {
        return geographicRepository.findByTypeAndIsActiveTrue(type)
                .stream()
                .map(geographicMapper::toResponse)
                .collect(Collectors.toList());
    }
}
