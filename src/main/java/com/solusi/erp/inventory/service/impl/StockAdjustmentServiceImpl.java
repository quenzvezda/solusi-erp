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
import com.solusi.erp.inventory.repository.ProductRepository;
import com.solusi.erp.inventory.repository.StockAdjustmentRepository;
import com.solusi.erp.inventory.service.StockAdjustmentService;
import com.solusi.erp.inventory.service.StockService;
import com.solusi.erp.master.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private final MessageSource messageSource;

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
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
            StockMovementPayload payload = StockMovementPayload.builder()
                    .productId(line.getProduct().getId())
                    .containerId(line.getContainer().getId())
                    .serialNumber(line.getSerialNumber())
                    .quantity(line.getQuantity())
                    .movementType(MovementType.ADJUSTMENT)
                    .referenceType(ReferenceType.STOCK_ADJUSTMENT)
                    .referenceId(entity.getId())
                    .referenceCode(entity.getCode())
                    .currencyId(entity.getTotalCost().getCurrency() != null ? entity.getTotalCost().getCurrency().getId() : null)
                    .exchangeRate(entity.getTotalCost().getExchangeRate())
                    .netPrice(line.getUnitCost())
                    .transactionDate(entity.getTransactionDate().atStartOfDay())
                    .build();

            stockService.adjust(payload);
        }

        entity.setStatus(AdjustmentStatus.COMPLETED);
        repository.save(entity);
    }

    private void populateLines(StockAdjustment entity, StockAdjustmentRequest request) {
        entity.getTotalCost().setCurrency(currencyRepository.getReferenceById(request.getCurrencyId()));
        request.getLines().forEach(lineReq -> {
            StockAdjustmentLine line = mapper.toLineEntity(lineReq);
            line.setHeader(entity);
            line.setProduct(productRepository.getReferenceById(lineReq.getProductId()));
            line.setContainer(containerRepository.getReferenceById(lineReq.getContainerId()));
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
