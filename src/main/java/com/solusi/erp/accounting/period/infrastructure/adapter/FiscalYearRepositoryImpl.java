package com.solusi.erp.accounting.period.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;
import com.solusi.erp.accounting.period.infrastructure.persistence.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FiscalYearRepositoryImpl implements FiscalYearRepository {

    private final FiscalYearJpaRepository fyJpaRepo;
    private final AccountingPeriodJpaRepository periodJpaRepo;
    private final FiscalYearPersistenceMapper fyMapper;
    private final PeriodPersistenceMapper periodMapper;

    public FiscalYearRepositoryImpl(FiscalYearJpaRepository fyJpaRepo,
                                     AccountingPeriodJpaRepository periodJpaRepo,
                                     FiscalYearPersistenceMapper fyMapper,
                                     PeriodPersistenceMapper periodMapper) {
        this.fyJpaRepo = fyJpaRepo;
        this.periodJpaRepo = periodJpaRepo;
        this.fyMapper = fyMapper;
        this.periodMapper = periodMapper;
    }

    @Override
    public FiscalYear save(FiscalYear domain) {
        var entity = fyMapper.toEntity(domain);
        var saved = fyJpaRepo.save(entity);
        return fyMapper.toDomain(saved);
    }

    @Override
    public Optional<FiscalYear> findById(Long id) {
        return fyJpaRepo.findById(id).map(fyMapper::toDomain);
    }

    @Override
    public Page<FiscalYear> findAll(String keyword, Pageable pageable) {
        var springPageable = PageableMapper.toSpring(pageable);
        var springPage = (keyword != null && !keyword.isBlank())
                ? fyJpaRepo.search(keyword, springPageable)
                : fyJpaRepo.findAllOrdered(springPageable);
        return new Page<>(
                springPage.getContent().stream().map(fy -> {
                    var domain = fyMapper.toDomain(fy);
                    var periods = periodJpaRepo.findByFiscalYearIdOrderByStartDate(fy.getId())
                            .stream().map(periodMapper::toDomain).toList();
                    return new FiscalYear(domain.getMetadata(), domain.getCode(), domain.getName(),
                            domain.getStartDate(), domain.getEndDate(), domain.getIsActive(), periods);
                }).collect(Collectors.toList()),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements()
        );
    }

    @Override
    public Optional<FiscalYear> findByPeriodId(Long periodId) {
        return periodJpaRepo.findById(periodId)
                .flatMap(periodEntity -> {
                    Long fyId = periodEntity.getFiscalYearId();
                    return fyJpaRepo.findById(fyId).map(fyEntity -> {
                        var fy = fyMapper.toDomain(fyEntity);
                        var periods = periodJpaRepo.findByFiscalYearIdOrderByStartDate(fyId)
                                .stream().map(periodMapper::toDomain).toList();
                        return new FiscalYear(fy.getMetadata(), fy.getCode(), fy.getName(),
                                fy.getStartDate(), fy.getEndDate(), fy.getIsActive(), periods);
                    });
                });
    }

    @Override
    public void delete(Long id) {
        periodJpaRepo.findByFiscalYearIdOrderByStartDate(id)
                .forEach(p -> periodJpaRepo.deleteById(p.getId()));
        fyJpaRepo.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return fyJpaRepo.findByCode(code).isPresent();
    }

    @Override
    public AccountingPeriod savePeriod(AccountingPeriod period) {
        var entity = periodMapper.toEntity(period);
        var saved = periodJpaRepo.save(entity);
        return periodMapper.toDomain(saved);
    }

    @Override
    public List<AccountingPeriod> savePeriods(List<AccountingPeriod> periods) {
        return periods.stream()
                .map(p -> {
                    var entity = periodMapper.toEntity(p);
                    var saved = periodJpaRepo.save(entity);
                    return periodMapper.toDomain(saved);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountingPeriod> findPeriodsByFiscalYearId(Long fiscalYearId) {
        return periodJpaRepo.findByFiscalYearIdOrderByStartDate(fiscalYearId)
                .stream().map(periodMapper::toDomain).collect(Collectors.toList());
    }
}
