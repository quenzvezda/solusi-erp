package com.solusi.erp.testutils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class TestPageBuilder {

    public static <T> Page<T> pageOf(List<T> content, Pageable pageable, long total) {
        return new PageImpl<>(content, pageable, total);
    }

    public static <T> Page<T> pageOf(List<T> content) {
        return new PageImpl<>(content);
    }
}
