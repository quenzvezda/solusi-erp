package com.solusi.erp.inventory.goodsreceipt.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.inventory.goodsreceipt.application.usecase.command.*;
import com.solusi.erp.inventory.goodsreceipt.application.usecase.query.*;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.web.dto.*;
import com.solusi.erp.inventory.goodsreceipt.web.mapper.GoodsReceiptWebMapper;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/inventory/goods-receipts")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class GoodsReceiptController {

    private final CreateGoodsReceiptUseCase createGoodsReceiptUseCase;
    private final UpdateGoodsReceiptUseCase updateGoodsReceiptUseCase;
    private final DeleteGoodsReceiptUseCase deleteGoodsReceiptUseCase;
    private final CompleteGoodsReceiptUseCase completeGoodsReceiptUseCase;
    private final FindGoodsReceiptsUseCase findGoodsReceiptsUseCase;
    private final GetGoodsReceiptUseCase getGoodsReceiptUseCase;
    private final GetGoodsReceiptEditViewUseCase getGoodsReceiptEditViewUseCase;
    private final GetGoodsReceiptCreateViewUseCase getGoodsReceiptCreateViewUseCase;
    private final GoodsReceiptWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('GOODS-RECEIPT_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        Page<GoodsReceipt> domainPage = findGoodsReceiptsUseCase.execute(keyword, domainPageable);

        List<GoodsReceiptSummaryResponse> content = domainPage.content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());

        org.springframework.data.domain.Page<GoodsReceiptSummaryResponse> springPage =
            new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "inventory/goods-receipts/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('GOODS-RECEIPT_CREATE')")
    public String createForm(@RequestParam(required = false) Long poId, Model model) {
        GoodsReceipt draftGr = getGoodsReceiptCreateViewUseCase.execute(poId);
        GoodsReceiptSaveRequest request = webMapper.toSaveRequest(draftGr);
        model.addAttribute("grRequest", request);
        return "inventory/goods-receipts/form";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GOODS-RECEIPT_READ')")
    public String view(@PathVariable Long id, Model model) {
        GoodsReceipt domain = getGoodsReceiptUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Goods receipt not found"));
        GoodsReceiptDetailResponse response = webMapper.toDetailResponse(domain);
        model.addAttribute("gr", response);
        return "inventory/goods-receipts/view";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('GOODS-RECEIPT_UPDATE')")
    public String editForm(@PathVariable Long id, Model model) {
        GoodsReceipt domain = getGoodsReceiptEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Goods receipt not found"));
        GoodsReceiptSaveRequest saveRequest = webMapper.toSaveRequest(domain);
        model.addAttribute("grRequest", saveRequest);
        return "inventory/goods-receipts/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('GOODS-RECEIPT_CREATE', 'GOODS-RECEIPT_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<GoodsReceiptDetailResponse>> save(
            @Valid @RequestBody GoodsReceiptSaveRequest request) {
        List<GoodsReceiptLineCommand> lines = webMapper.toLineCommands(request.getLines());

        GoodsReceipt domain;
        if (request.getId() == null) {
            domain = createGoodsReceiptUseCase.execute(
                request.getReceiptDate(),
                null,
                request.getNotes(),
                lines
            );
            GoodsReceiptDetailResponse data = webMapper.toDetailResponse(domain);
            String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
        } else {
            domain = updateGoodsReceiptUseCase.execute(
                request.getId(),
                request.getReceiptDate(),
                request.getNotes(),
                lines
            );
            GoodsReceiptDetailResponse data = webMapper.toDetailResponse(domain);
            String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
            return ResponseEntity.ok(ApiResponse.success(msg, data));
        }
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('GOODS-RECEIPT_COMPLETE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<GoodsReceiptDetailResponse>> complete(@PathVariable Long id) {
        completeGoodsReceiptUseCase.execute(id);
        GoodsReceipt domain = getGoodsReceiptUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Goods receipt not found"));
        GoodsReceiptDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.gr.completed", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GOODS-RECEIPT_DELETE')")
    public org.springframework.http.ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteGoodsReceiptUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }
}
