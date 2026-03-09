package com.solusi.erp.master.service;

import com.solusi.erp.master.dto.GeographicDto;
import com.solusi.erp.master.model.GeographicType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GeographicService {
    Page<GeographicDto> getAllGeographics(String keyword, Pageable pageable);
    
    Page<GeographicDto> getByParent(Long parentId, Pageable pageable);
    
    GeographicDto getById(Long id);
    
    GeographicDto create(GeographicDto dto);
    
    GeographicDto update(Long id, GeographicDto dto);
    
    void delete(Long id);
    
    List<GeographicDto> getByType(GeographicType type);
    
    List<GeographicDto> getByParentActive(Long parentId);
}
