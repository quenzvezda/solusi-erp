package com.solusi.erp.core.util;

import com.solusi.erp.core.domain.model.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class PageableMapper {

    private PageableMapper() {
    }

    public static Pageable toDomain(org.springframework.data.domain.Pageable springPageable) {
        String sortField = null;
        String sortDir = null;
        if (springPageable != null && springPageable.getSort().isSorted()) {
            Sort.Order order = springPageable.getSort().iterator().next();
            sortField = order.getProperty();
            sortDir = order.getDirection().name().toLowerCase();
        }
        return Pageable.of(
                springPageable.getPageNumber(),
                springPageable.getPageSize(),
                sortField,
                sortDir
        );
    }

    public static org.springframework.data.domain.Pageable toSpring(Pageable pageable) {
        if (pageable != null && pageable.isSorted()) {
            Sort sort = Sort.by("desc".equalsIgnoreCase(pageable.sortDir())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC, pageable.sortField());
            return PageRequest.of(pageable.page(), pageable.size(), sort);
        }
        return PageRequest.of(pageable.page(), pageable.size());
    }
}
