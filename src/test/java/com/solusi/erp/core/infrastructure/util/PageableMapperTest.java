package com.solusi.erp.core.infrastructure.util;

import com.solusi.erp.core.domain.model.Pageable;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class PageableMapperTest {

    @Test
    void toDomain_sortedPageable_mapsSortFields() {
        var spring = PageRequest.of(2, 15, Sort.by(Sort.Direction.DESC, "createdDate"));

        Pageable domain = PageableMapper.toDomain(spring);

        assertThat(domain.page()).isEqualTo(2);
        assertThat(domain.size()).isEqualTo(15);
        assertThat(domain.sortField()).isEqualTo("createdDate");
        assertThat(domain.sortDir()).isEqualTo("desc");
        assertThat(domain.isSorted()).isTrue();
    }

    @Test
    void toDomain_unsortedPageable_noSortFields() {
        var spring = PageRequest.of(0, 10);

        Pageable domain = PageableMapper.toDomain(spring);

        assertThat(domain.page()).isZero();
        assertThat(domain.size()).isEqualTo(10);
        assertThat(domain.sortField()).isNull();
        assertThat(domain.isSorted()).isFalse();
    }

    @Test
    void toSpring_sortedDomainPageable_mapsSortFields() {
        Pageable domain = Pageable.of(1, 20, "name", "asc");

        var spring = PageableMapper.toSpring(domain);

        assertThat(spring.getPageNumber()).isEqualTo(1);
        assertThat(spring.getPageSize()).isEqualTo(20);
        assertThat(spring.getSort().isSorted()).isTrue();
        Sort.Order order = spring.getSort().iterator().next();
        assertThat(order.getProperty()).isEqualTo("name");
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void toSpring_unsortedDomainPageable_noSort() {
        Pageable domain = Pageable.of(0, 10);

        var spring = PageableMapper.toSpring(domain);

        assertThat(spring.getPageNumber()).isZero();
        assertThat(spring.getPageSize()).isEqualTo(10);
        assertThat(spring.getSort().isSorted()).isFalse();
    }

    @Test
    void roundTrip_sortedPageable_preservesValues() {
        var original = PageRequest.of(3, 25, Sort.by(Sort.Direction.DESC, "updatedDate"));

        Pageable domain = PageableMapper.toDomain(original);
        var roundTripped = PageableMapper.toSpring(domain);

        assertThat(roundTripped.getPageNumber()).isEqualTo(original.getPageNumber());
        assertThat(roundTripped.getPageSize()).isEqualTo(original.getPageSize());
        assertThat(roundTripped.getSort().iterator().next().getProperty())
                .isEqualTo(original.getSort().iterator().next().getProperty());
        assertThat(roundTripped.getSort().iterator().next().getDirection())
                .isEqualTo(original.getSort().iterator().next().getDirection());
    }

    @Test
    void roundTrip_unsortedPageable_preservesValues() {
        var original = PageRequest.of(0, 10);

        Pageable domain = PageableMapper.toDomain(original);
        var roundTripped = PageableMapper.toSpring(domain);

        assertThat(roundTripped.getPageNumber()).isEqualTo(original.getPageNumber());
        assertThat(roundTripped.getPageSize()).isEqualTo(original.getPageSize());
        assertThat(roundTripped.getSort().isSorted()).isFalse();
    }
}
