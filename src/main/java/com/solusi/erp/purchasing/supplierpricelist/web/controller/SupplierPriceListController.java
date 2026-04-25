package com.solusi.erp.purchasing.supplierpricelist.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.purchasing.supplierpricelist.application.usecase.command.*;
import com.solusi.erp.purchasing.supplierpricelist.application.usecase.query.*;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.*;
import com.solusi.erp.purchasing.supplierpricelist.web.mapper.SupplierPriceListWebMapper;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/purchasing/supplier-price-lists")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class SupplierPriceListController {

    private final CreateSupplierPriceListUseCase createSupplierPriceListUseCase;
    private final UpdateSupplierPriceListUseCase updateSupplierPriceListUseCase;
    private final DeleteSupplierPriceListUseCase deleteSupplierPriceListUseCase;
    private final FindSupplierPriceListsUseCase findSupplierPriceListsUseCase;
    private final GetSupplierPriceListEditViewUseCase getSupplierPriceListEditViewUseCase;
    private final SupplierPriceListWebMapper webMapper;
    private final MessageSource messageSource;
    private final PartyLookupProvider partyLookupProvider;
    private final ProductLookupProvider productLookupProvider;
    private final UomLookupProvider uomLookupProvider;
    private final CurrencyLookupProvider currencyLookupProvider;

    @GetMapping
    @PreAuthorize("hasAuthority('SPL_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<SupplierPriceList> domainPage =
            findSupplierPriceListsUseCase.execute(keyword, domainPageable);

        List<SupplierPriceListSummaryResponse> content = domainPage.content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());

        Page<SupplierPriceListSummaryResponse> springPage =
            new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "purchasing/supplier-price-lists/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('SPL_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("splRequest", new SupplierPriceListSaveRequest());
        return "purchasing/supplier-price-lists/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('SPL_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<SupplierPriceListDetailResponse>> create(
            @Valid @RequestBody SupplierPriceListSaveRequest request) {
        SupplierPriceList domain = createSupplierPriceListUseCase.execute(
            request.getSupplierId(), request.getProductId(),
            request.getUomId(), request.getCurrencyId(),
            request.getUnitPrice(), request.getMinQuantity(),
            request.getEffectiveFrom(), request.getEffectiveTo(),
            request.getNote(), request.isActive()
        );
        SupplierPriceListDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('SPL_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        SupplierPriceList domain = getSupplierPriceListEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Supplier price list not found"));
        model.addAttribute("splRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        model.addAttribute("splUI", buildSPLUI(domain));
        return "purchasing/supplier-price-lists/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('SPL_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<SupplierPriceListDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierPriceListSaveRequest request) {
        SupplierPriceList domain = updateSupplierPriceListUseCase.execute(
            id, request.getSupplierId(), request.getProductId(), request.getUomId(), request.getCurrencyId(),
            request.getUnitPrice(), request.getMinQuantity(),
            request.getEffectiveFrom(), request.getEffectiveTo(),
            request.getNote(), request.isActive()
        );
        SupplierPriceListDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SPL_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteSupplierPriceListUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    private Map<String, Object> buildSPLUI(SupplierPriceList domain) {
        Map<String, Object> ui = new HashMap<>();

        LookupDto supplier = partyLookupProvider.resolve(domain.getSupplierId());
        if (supplier != null) {
            ui.put("supplierText", supplier.name());
            ui.put("supplierSubtext", supplier.subText());
        }

        LookupDto product = productLookupProvider.resolve(domain.getProductId());
        if (product != null) {
            ui.put("productText", product.name());
            ui.put("productSubtext", product.subText());
        }

        LookupDto uom = uomLookupProvider.resolve(domain.getUomId());
        if (uom != null) {
            ui.put("uomText", uom.name());
            ui.put("uomSubtext", uom.subText());
        }

        LookupDto currency = currencyLookupProvider.resolve(domain.getCurrencyId());
        if (currency != null) {
            ui.put("currencyText", currency.name());
            ui.put("currencySubtext", currency.subText());
        }

        return ui;
    }
}
