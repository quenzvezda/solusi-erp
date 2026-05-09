package com.solusi.erp.accounting.coa.application.usecase.query;

public record CoaSelectorRow(
        Long id,
        String code,
        String name,
        String accountType,
        Integer level,
        Boolean isHeader,
        Long parentId,
        String parentCode,
        String parentName
) {
}
