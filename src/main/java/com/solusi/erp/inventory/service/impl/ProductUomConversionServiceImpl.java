package com.solusi.erp.inventory.service.impl;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.inventory.dto.ProductUomConversionRequest;
import com.solusi.erp.inventory.dto.ProductUomConversionResponse;
import com.solusi.erp.inventory.form.ProductUomUIForm;
import com.solusi.erp.inventory.mapper.ProductUomConversionMapper;
import com.solusi.erp.inventory.model.Product;
import com.solusi.erp.inventory.model.ProductUomConversion;
import com.solusi.erp.inventory.repository.ProductRepository;
import com.solusi.erp.inventory.repository.ProductUomConversionRepository;
import com.solusi.erp.inventory.repository.UnitOfMeasureRepository;
import com.solusi.erp.inventory.service.ProductUomConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductUomConversionServiceImpl implements ProductUomConversionService {

    private final ProductUomConversionRepository repository;
    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository uomRepository;
    private final ProductUomConversionMapper mapper;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductUomConversionResponse> findAll(String keyword, Pageable pageable) {
        return repository.search(keyword, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductUomConversionResponse findById(Long id) {
        ProductUomConversion entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductUomConversionRequest getEditData(Long id) {
        ProductUomConversion entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
        ProductUomConversionRequest request = mapper.toRequest(entity);
        if (request.getConversionFactor() != null) {
            request.setConversionFactor(request.getConversionFactor().setScale(2, java.math.RoundingMode.HALF_UP));
        }
        return request;
    }

    @Override
    @Transactional(readOnly = true)
    public FormViewDto<ProductUomConversionRequest, ProductUomUIForm, ProductUomConversionResponse> getFormView(Long id) {
        ProductUomConversion entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));

        return FormViewDto.<ProductUomConversionRequest, ProductUomUIForm, ProductUomConversionResponse>builder()
                .request(getEditData(id))
                .ui(mapper.toUIForm(entity))
                .audit(mapper.toResponse(entity))
                .build();
    }

    @Override
    @Transactional
    public ProductUomConversionResponse create(ProductUomConversionRequest request) {
        validateRequest(request, null);
        
        ProductUomConversion entity = mapper.toEntity(request);
        BigDecimal factor = request.getConversionFactor();
        if (factor != null) {
            factor = factor.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        entity.setConversionFactor(factor);
        populateAssociations(entity, request);
        
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public ProductUomConversionResponse update(Long id, ProductUomConversionRequest request) {
        ProductUomConversion entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
        
        validateRequest(request, id);
        
        mapper.updateEntityFromRequest(request, entity);
        
        BigDecimal factor = request.getConversionFactor();
        if (factor != null) {
            factor = factor.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        entity.setConversionFactor(factor);
        populateAssociations(entity, request);

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException(getMessage("msg.error.notfound"));
        }
        repository.deleteById(id);
    }

    private void validateRequest(ProductUomConversionRequest request, Long id) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.product.notfound")));

        if (request.getConversionFactor() == null || request.getConversionFactor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(getMessage("msg.error.uom.conversion.factor_positive"));
        }

        if (request.getFromUomId().equals(product.getUom().getId())) {
            throw new RuntimeException(getMessage("msg.error.uom.conversion.self_conversion"));
        }

        boolean exists = (id == null) ? 
                repository.existsByProductIdAndFromUomId(request.getProductId(), request.getFromUomId()) :
                repository.existsByProductIdAndFromUomIdAndIdNot(request.getProductId(), request.getFromUomId(), id);
        
        if (exists) {
            throw new RuntimeException(getMessage("msg.error.uom.conversion.duplicate", product.getName()));
        }
    }

    private void populateAssociations(ProductUomConversion entity, ProductUomConversionRequest request) {
        Product product = productRepository.getReferenceById(request.getProductId());
        entity.setProduct(product);
        entity.setFromUom(uomRepository.getReferenceById(request.getFromUomId()));
        entity.setToUom(product.getUom()); // Always convert to Base UOM
    }

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
