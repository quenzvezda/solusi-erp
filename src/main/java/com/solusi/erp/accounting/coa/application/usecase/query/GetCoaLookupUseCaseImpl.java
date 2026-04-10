package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;

import java.util.List;
import java.util.stream.Collectors;

public class GetCoaLookupUseCaseImpl implements GetCoaLookupUseCase {

    private final CoaRepository repository;

    public GetCoaLookupUseCaseImpl(CoaRepository repository) {
        this.repository = repository;
    }

    @Override
    public LookupDto getById(Long id) {
        ChartOfAccount coa = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.coa.notfound"));
        return toLookupDto(coa);
    }

    @Override
    public List<LookupDto> search(String keyword, int limit) {
        return repository.search(keyword, limit).stream()
                .map(this::toLookupDto)
                .collect(Collectors.toList());
    }

    private LookupDto toLookupDto(ChartOfAccount coa) {
        return new LookupDto(coa.getId(), coa.getName(), coa.getCode());
    }
}
