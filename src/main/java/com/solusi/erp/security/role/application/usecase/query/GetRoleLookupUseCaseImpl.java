package com.solusi.erp.security.role.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.role.domain.repository.RoleRepository;

import java.util.List;
import java.util.stream.Collectors;

public class GetRoleLookupUseCaseImpl implements GetRoleLookupUseCase {

    private final RoleRepository repository;

    public GetRoleLookupUseCaseImpl(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public LookupDto getById(Long id) {
        Role role = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.role.notfound"));
        return toLookupDto(role);
    }

    @Override
    public List<LookupDto> search(String keyword, int limit) {
        return repository.search(keyword, limit).stream()
                .map(this::toLookupDto)
                .collect(Collectors.toList());
    }

    private LookupDto toLookupDto(Role role) {
        return new LookupDto(role.getId(), role.getName(), role.getDescription());
    }
}
