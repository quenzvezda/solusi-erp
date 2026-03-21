package com.solusi.erp.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A Generic DTO to encapsulate all data needed for any Edit/Create form.
 * Solves LazyInitializationException by allowing mapping to happen inside the transactional service layer.
 * 
 * @param <RQ> Request DTO type (for form binding)
 * @param <UI> UI Metadata type (for labels/display info)
 * @param <RP> Response DTO type (for audit info display)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormViewDto<RQ, UI, RP> {
    private RQ request;
    private UI ui;
    private RP audit;
}
