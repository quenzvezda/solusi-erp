package com.solusi.erp.accounting.schema.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.accounting.schema.application.usecase.command.CreateSchemaUseCase;
import com.solusi.erp.accounting.schema.application.usecase.command.DeleteSchemaUseCase;
import com.solusi.erp.accounting.schema.application.usecase.command.UpdateSchemaUseCase;
import com.solusi.erp.accounting.schema.application.usecase.query.FindSchemasUseCase;
import com.solusi.erp.accounting.schema.application.usecase.query.GetSchemaEditViewUseCase;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.web.dto.SchemaDetailResponse;
import com.solusi.erp.accounting.schema.web.dto.SchemaSaveRequest;
import com.solusi.erp.accounting.schema.web.dto.SchemaSummaryResponse;
import com.solusi.erp.accounting.schema.web.mapper.SchemaWebMapper;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/accounting/schemas")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class SchemaController {

    private final CreateSchemaUseCase createSchemaUseCase;
    private final UpdateSchemaUseCase updateSchemaUseCase;
    private final DeleteSchemaUseCase deleteSchemaUseCase;
    private final FindSchemasUseCase findSchemasUseCase;
    private final GetSchemaEditViewUseCase getSchemaEditViewUseCase;
    private final SchemaWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNTING-SCHEMA_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<AccountingSchema> domainPage = findSchemasUseCase.execute(keyword, domainPageable);

        List<SchemaSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<SchemaSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("eventTypes", SchemaEventType.values());
        return "accounting/schema/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('ACCOUNTING-SCHEMA_CREATE')")
    public String showCreateForm(Model model) {
        SchemaSaveRequest request = new SchemaSaveRequest();
        request.setIsActive(true);
        model.addAttribute("schemaRequest", request);
        model.addAttribute("eventTypes", SchemaEventType.values());
        return "accounting/schema/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ACCOUNTING-SCHEMA_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<SchemaDetailResponse>> create(@Valid @RequestBody SchemaSaveRequest request) {
        SchemaEventType eventType;
        try {
            eventType = SchemaEventType.valueOf(request.getEventType());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid event type: " + request.getEventType()));
        }
        AccountingSchema domain = createSchemaUseCase.execute(
                eventType,
                request.getDescription(),
                request.getDebitAccountId(), request.getCreditAccountId(),
                request.getIsActive());
        SchemaDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-SCHEMA_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        AccountingSchema domain = getSchemaEditViewUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Schema not found"));
        model.addAttribute("schemaRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        model.addAttribute("eventTypes", SchemaEventType.values());
        return "accounting/schema/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-SCHEMA_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<SchemaDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SchemaSaveRequest request) {
        AccountingSchema domain = updateSchemaUseCase.execute(
                id, request.getDescription(),
                request.getDebitAccountId(), request.getCreditAccountId(),
                request.getIsActive());
        SchemaDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-SCHEMA_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DeleteResult result = deleteSchemaUseCase.execute(id);
        if (result == DeleteResult.SOFT_DELETED) {
            String msg = messageSource.getMessage("msg.success.deactivated", null, LocaleContextHolder.getLocale());
            return HtmxResponseUtility.okWithRefreshTableAndWarning(msg);
        }
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }
}
