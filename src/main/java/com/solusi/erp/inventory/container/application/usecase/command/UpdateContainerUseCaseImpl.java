package com.solusi.erp.inventory.container.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.container.domain.model.Container;
import com.solusi.erp.inventory.container.domain.repository.ContainerRepository;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;

public class UpdateContainerUseCaseImpl implements UpdateContainerUseCase {

    private final ContainerRepository repository;

    public UpdateContainerUseCaseImpl(ContainerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Container execute(Long id, String name, String barcode, BigDecimal length, BigDecimal width,
                             BigDecimal height, BigDecimal maxWeight, String note, Boolean isActive) {
        Container container = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.container.notfound"));
        if (StringUtils.hasText(barcode) && repository.existsByBarcodeAndIdNot(barcode, id)) {
            throw new DomainException("msg.error.container.duplicate-barcode");
        }
        container.update(name, barcode, length, width, height, maxWeight, note, isActive);
        return repository.save(container);
    }
}
