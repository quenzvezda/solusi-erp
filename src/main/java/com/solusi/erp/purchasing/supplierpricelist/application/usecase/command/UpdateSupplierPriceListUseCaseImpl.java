package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateSupplierPriceListUseCaseImpl implements UpdateSupplierPriceListUseCase {

    private final SupplierPriceListRepository repository;

    public UpdateSupplierPriceListUseCaseImpl(SupplierPriceListRepository repository) {
        this.repository = repository;
    }

    @Override
    public SupplierPriceList execute(Long id, Long supplierId, Long productId, Long uomId, Long currencyId,
                                       BigDecimal unitPrice, BigDecimal minQuantity,
                                       LocalDate effectiveFrom, LocalDate effectiveTo,
                                       String note, boolean active) {
        SupplierPriceList spl = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.spl.notfound"));

        if (repository.existsOverlapping(supplierId, productId, uomId, currencyId,
                effectiveFrom, effectiveTo, id)) {
            throw new DomainException("msg.error.spl.overlap");
        }

        spl.update(productId, uomId, currencyId, supplierId, unitPrice, minQuantity,
            effectiveFrom, effectiveTo, note, active);
        return repository.save(spl);
    }
}
