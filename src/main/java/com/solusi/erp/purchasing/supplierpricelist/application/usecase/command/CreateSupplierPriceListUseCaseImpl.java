package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateSupplierPriceListUseCaseImpl implements CreateSupplierPriceListUseCase {

    private final SupplierPriceListRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreateSupplierPriceListUseCaseImpl(SupplierPriceListRepository repository,
                                               SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public SupplierPriceList execute(Long supplierId, Long productId, Long uomId, Long currencyId,
                                      BigDecimal unitPrice, BigDecimal minQuantity,
                                      LocalDate effectiveFrom, LocalDate effectiveTo,
                                      String note, boolean active) {
        String code = sequenceGeneratorService.generate("SPL");

        if (repository.existsOverlapping(supplierId, productId, uomId, currencyId,
                effectiveFrom, effectiveTo, null)) {
            throw new DomainException("msg.error.spl.overlap");
        }

        SupplierPriceList spl = SupplierPriceList.createNew(
            code, supplierId, productId, uomId, currencyId,
            unitPrice, minQuantity, effectiveFrom, effectiveTo, note, active
        );
        return repository.save(spl);
    }
}
