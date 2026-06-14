package com.solusi.erp.accountspayable.vendorbill.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillDocumentStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillSettlementStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence.VendorBillEntity;
import com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence.VendorBillJpaRepository;
import com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence.VendorBillLineEntity;
import com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence.VendorBillPersistenceMapper;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;

import java.util.List;
import java.util.Optional;

public class VendorBillRepositoryImpl implements VendorBillRepository {

    private final VendorBillJpaRepository jpaRepository;
    private final VendorBillPersistenceMapper mapper;

    public VendorBillRepositoryImpl(VendorBillJpaRepository jpaRepository, VendorBillPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<VendorBill> findAll(String keyword, Long vendorId, VendorBillDocumentStatus documentStatus,
                                    VendorBillSettlementStatus settlementStatus, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        org.springframework.data.domain.Page<VendorBillEntity> springPage =
                jpaRepository.findAllFiltered(normalizedKeyword, vendorId, documentStatus, settlementStatus, springPageable);

        return new Page<>(
                springPage.getContent().stream().map(mapper::toDomain).toList(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements()
        );
    }

    @Override
    public VendorBill save(VendorBill bill) {
        VendorBillEntity entity = mapper.toEntity(bill);
        entity.getLines().clear();
        List<VendorBillLineEntity> lineEntities = mapper.toLineEntityList(bill.getLines(), entity);
        entity.getLines().addAll(lineEntities);

        entity.getGrRefs().clear();
        if (entity.getId() == null && !bill.getGrRefs().isEmpty()) {
            VendorBillEntity savedHeader = jpaRepository.save(entity);
            savedHeader.getGrRefs().clear();
            savedHeader.getGrRefs().addAll(mapper.toGrRefEntityList(bill.getGrRefs(), savedHeader));
            VendorBillEntity savedWithRefs = jpaRepository.save(savedHeader);
            return mapper.toDomain(savedWithRefs);
        }

        entity.getGrRefs().addAll(mapper.toGrRefEntityList(bill.getGrRefs(), entity));

        VendorBillEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<VendorBill> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
