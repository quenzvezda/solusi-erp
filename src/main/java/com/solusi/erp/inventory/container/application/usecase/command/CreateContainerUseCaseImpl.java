package com.solusi.erp.inventory.container.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.container.domain.model.Container;
import com.solusi.erp.inventory.container.domain.repository.ContainerRepository;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;

public class CreateContainerUseCaseImpl implements CreateContainerUseCase {

    private final ContainerRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreateContainerUseCaseImpl(ContainerRepository repository, SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public Container execute(Long gridId, String name, String barcode, BigDecimal length, BigDecimal width,
                             BigDecimal height, BigDecimal maxWeight, String note, Boolean isActive) {
        if (StringUtils.hasText(barcode) && repository.existsByBarcode(barcode)) {
            throw new DomainException("msg.error.container.duplicate-barcode");
        }
        String code = sequenceGeneratorService.generate("CONTAINER");
        Container container = Container.createNew(gridId, name, barcode, length, width, height, maxWeight, note, isActive);
        container.assignCode(code);
        return repository.save(container);
    }
}
