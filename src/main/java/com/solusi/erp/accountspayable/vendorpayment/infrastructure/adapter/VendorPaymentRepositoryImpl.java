package com.solusi.erp.accountspayable.vendorpayment.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentStatus;
import com.solusi.erp.accountspayable.vendorpayment.domain.repository.VendorPaymentRepository;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.persistence.VendorPaymentEntity;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.persistence.VendorPaymentJpaRepository;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.persistence.VendorPaymentLineEntity;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.persistence.VendorPaymentPersistenceMapper;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;

import java.util.List;
import java.util.Optional;

public class VendorPaymentRepositoryImpl implements VendorPaymentRepository {

    private final VendorPaymentJpaRepository jpaRepository;
    private final VendorPaymentPersistenceMapper mapper;

    public VendorPaymentRepositoryImpl(VendorPaymentJpaRepository jpaRepository,
                                        VendorPaymentPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<VendorPayment> findAll(String keyword, Long vendorId, VendorPaymentStatus status, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        org.springframework.data.domain.Page<VendorPaymentEntity> springPage =
                jpaRepository.findAllFiltered(normalizedKeyword, vendorId, status, springPageable);
        return new Page<>(
                springPage.getContent().stream().map(mapper::toDomain).toList(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements()
        );
    }

    @Override
    public VendorPayment save(VendorPayment payment) {
        VendorPaymentEntity entity = mapper.toEntity(payment);
        entity.getLines().clear();
        List<VendorPaymentLineEntity> lineEntities = mapper.toLineEntityList(payment.getLines(), entity);
        entity.getLines().addAll(lineEntities);
        VendorPaymentEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<VendorPayment> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
