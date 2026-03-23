package com.solusi.erp.inventory.service.impl;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.dto.StockAdjustmentRequest;
import com.solusi.erp.inventory.dto.StockAdjustmentResponse;
import com.solusi.erp.inventory.dto.StockMovementPayload;
import com.solusi.erp.inventory.mapper.StockAdjustmentMapper;
import com.solusi.erp.inventory.model.MovementType;
import com.solusi.erp.inventory.model.ReferenceType;
import com.solusi.erp.inventory.model.StockAdjustment;
import com.solusi.erp.inventory.model.StockAdjustment.AdjustmentStatus;
import com.solusi.erp.inventory.model.StockAdjustmentLine;
import com.solusi.erp.inventory.repository.ContainerRepository;
import com.solusi.erp.inventory.repository.FacilityRepository;
import com.solusi.erp.inventory.repository.GridRepository;
import com.solusi.erp.inventory.repository.ProductRepository;
import com.solusi.erp.inventory.repository.StockAdjustmentRepository;
import com.solusi.erp.inventory.service.StockAdjustmentService;
import com.solusi.erp.inventory.service.StockService;
import com.solusi.erp.inventory.util.SerialNumberGenerator;
import com.solusi.erp.master.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockAdjustmentServiceImpl implements StockAdjustmentService {

    private final StockAdjustmentRepository repository;
    private final StockAdjustmentMapper mapper;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final StockService stockService;
    private final ProductRepository productRepository;
    private final ContainerRepository containerRepository;
    private final CurrencyRepository currencyRepository;
    private final FacilityRepository facilityRepository;
    private final GridRepository gridRepository;
    private final com.solusi.erp.inventory.repository.UnitOfMeasureRepository uomRepository;
    private final MessageSource messageSource;

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    @Override
    @Transactional(readOnly = true)
    public StockAdjustmentRequest getEditData(Long id) {
        StockAdjustment entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
        return mapper.toRequest(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockAdjustmentResponse> findAll(String keyword, Pageable pageable) {
        return repository.search(keyword, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public StockAdjustmentResponse findById(Long id) {
        StockAdjustment entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public StockAdjustmentResponse create(StockAdjustmentRequest request) {
        StockAdjustment entity = mapper.toEntity(request);
        entity.setCode(sequenceGeneratorService.generate("STOCK_ADJUSTMENT"));
        entity.setStatus(AdjustmentStatus.DRAFT);
        entity.setFacility(facilityRepository.getReferenceById(request.getFacilityId()));
        
        // Populate associations for lines
        populateLines(entity, request);
        calculateTotals(entity);
        
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public StockAdjustmentResponse update(Long id, StockAdjustmentRequest request) {
        StockAdjustment entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));

        if (entity.getStatus() == AdjustmentStatus.COMPLETED) {
            throw new RuntimeException(getMessage("msg.error.stock-adjustment.already-completed"));
        }

        StockAdjustment updated = mapper.toEntity(request);
        entity.setTransactionDate(updated.getTransactionDate());
        entity.setNote(updated.getNote());
        entity.setTotalCost(updated.getTotalCost());
        entity.setFacility(facilityRepository.getReferenceById(request.getFacilityId()));
        
        // Clear and repopulate lines
        entity.getLines().clear();
        populateLines(entity, request);
        calculateTotals(entity);

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        StockAdjustment entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));

        if (entity.getStatus() == AdjustmentStatus.COMPLETED) {
            throw new RuntimeException(getMessage("msg.error.stock-adjustment.already-completed"));
        }
        repository.delete(entity);
    }

    @Override
    @Transactional
    public void process(Long id) {
        StockAdjustment entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));

        if (entity.getStatus() == AdjustmentStatus.COMPLETED) {
            throw new RuntimeException(getMessage("msg.error.stock-adjustment.already-completed"));
        }

        for (StockAdjustmentLine line : entity.getLines()) {
            if (Boolean.TRUE.equals(line.getProduct().getIsSerialized())) {
                processSerializedLine(entity, line);
            } else {
                processStandardLine(entity, line);
            }
        }

        entity.setStatus(AdjustmentStatus.COMPLETED);
        repository.save(entity);
    }

    private void processSerializedLine(StockAdjustment entity, StockAdjustmentLine line) {
        // Explode into 1.0 units
        int totalUnits = line.getQuantity().abs().intValue();
        if (totalUnits == 0) return;

        String[] providedSns = StringUtils.hasText(line.getSerialNumber())
                ? line.getSerialNumber().split(",") 
                : new String[0];
        
        List<String> finalSns = new java.util.ArrayList<>();

        for (int i = 0; i < totalUnits; i++) {
            String sn = null;
            if (i < providedSns.length) {
                sn = providedSns[i].trim();
            } else if (line.getQuantity().signum() > 0) {
                // Auto-generate if adding stock and SN is missing
                sn = SerialNumberGenerator.generate();
            }

            if (sn != null) finalSns.add(sn);

            BigDecimal unitQty = line.getQuantity().signum() < 0 ? new BigDecimal("-1") : BigDecimal.ONE;
            
            StockMovementPayload payload = buildBasePayload(entity, line);
            payload.setQuantity(unitQty);
            payload.setSerialNumber(sn);
            
            stockService.adjust(payload);
        }

        // Back-fill the generated serial numbers into the adjustment line
        if (!finalSns.isEmpty()) {
            line.setSerialNumber(String.join(",", finalSns));
        }
    }

    private void processStandardLine(StockAdjustment entity, StockAdjustmentLine line) {
        StockMovementPayload payload = buildBasePayload(entity, line);
        payload.setQuantity(line.getQuantity());
        payload.setSerialNumber(line.getSerialNumber());
        stockService.adjust(payload);
    }

    private StockMovementPayload buildBasePayload(StockAdjustment entity, StockAdjustmentLine line) {
        return StockMovementPayload.builder()
                .productId(line.getProduct().getId())
                .containerId(line.getContainer().getId())
                .movementType(MovementType.ADJUSTMENT)
                .referenceType(ReferenceType.STOCK_ADJUSTMENT)
                .referenceId(entity.getId())
                .referenceCode(entity.getCode())
                .currencyId(entity.getTotalCost().getCurrency() != null ? entity.getTotalCost().getCurrency().getId() : null)
                .exchangeRate(entity.getTotalCost().getExchangeRate())
                .netPrice(line.getUnitCost())
                .transactionDate(entity.getTransactionDate().atStartOfDay())
                .build();
    }

    private void populateLines(StockAdjustment entity, StockAdjustmentRequest request) {
        if (entity.getTotalCost() == null) {
            entity.setTotalCost(new com.solusi.erp.core.model.CurrencyAmount());
        }
        entity.getTotalCost().setCurrency(currencyRepository.getReferenceById(request.getCurrencyId()));
        entity.getTotalCost().setExchangeRate(request.getExchangeRate());
        
        request.getLines().forEach(lineReq -> {
            StockAdjustmentLine line = mapper.toLineEntity(lineReq);
            line.setHeader(entity);
            line.setProduct(productRepository.getReferenceById(lineReq.getProductId()));
            line.setContainer(containerRepository.getReferenceById(lineReq.getContainerId()));
            if (lineReq.getGridId() != null) {
                line.setGrid(gridRepository.getReferenceById(lineReq.getGridId()));
            }
            if (lineReq.getUomId() != null) {
                line.setUom(uomRepository.getReferenceById(lineReq.getUomId()));
            }
            line.setTotalAmount(line.getQuantity().multiply(line.getUnitCost()));
            entity.getLines().add(line);
        });
    }

    private void calculateTotals(StockAdjustment entity) {
        BigDecimal totalOriginal = entity.getLines().stream()
                .map(StockAdjustmentLine::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal exchangeRate = entity.getTotalCost().getExchangeRate() != null ? 
                entity.getTotalCost().getExchangeRate() : BigDecimal.ONE;
        
        entity.getTotalCost().setOriginalAmount(totalOriginal);
        entity.getTotalCost().setLocalAmount(totalOriginal.multiply(exchangeRate));
    }
}
