package com.solusi.erp.accountspayable.vendorbill.web.controller;

import com.solusi.erp.accountspayable.vendorbill.application.usecase.command.*;
import com.solusi.erp.accountspayable.vendorbill.application.usecase.query.*;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrLineView;
import com.solusi.erp.accountspayable.vendorbill.web.dto.*;
import com.solusi.erp.accountspayable.vendorbill.web.mapper.VendorBillWebMapper;
import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
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

import java.util.List;

@Controller
@RequestMapping("/accounts-payable/vendor-bills")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class VendorBillController {

    private final CreateVendorBillUseCase createVendorBillUseCase;
    private final UpdateVendorBillUseCase updateVendorBillUseCase;
    private final DeleteVendorBillUseCase deleteVendorBillUseCase;
    private final CancelVendorBillUseCase cancelVendorBillUseCase;
    private final ConfirmVendorBillUseCase confirmVendorBillUseCase;
    private final FindVendorBillsUseCase findVendorBillsUseCase;
    private final GetVendorBillDetailUseCase getVendorBillDetailUseCase;
    private final GetVendorBillCreateViewUseCase getVendorBillCreateViewUseCase;
    private final FindBillableGrLinesUseCase findBillableGrLinesUseCase;
    private final FindBillableReferencesUseCase findBillableReferencesUseCase;
    private final VendorBillWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('VENDOR-BILL_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long vendorId,
                       @RequestParam(required = false) VendorBillStatus status,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<VendorBillSummaryView> domainPage =
                findVendorBillsUseCase.execute(keyword, vendorId, status, domainPageable);
        List<VendorBillSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .toList();

        model.addAttribute("page", new PageImpl<>(content, springPageable, domainPage.totalElements()));
        model.addAttribute("keyword", keyword);
        model.addAttribute("vendorId", vendorId);
        model.addAttribute("status", status);
        model.addAttribute("statuses", VendorBillStatus.values());
        return "accountspayable/vendor-bills/list";
    }

    @GetMapping("/select-references")
    @PreAuthorize("hasAuthority('VENDOR-BILL_CREATE')")
    public String selectReferences(@RequestParam(required = false) Long vendorId,
                                   @RequestParam(required = false) Long currencyId,
                                   Model model) {
        model.addAttribute("references", findBillableReferencesUseCase.execute(vendorId, currencyId));
        model.addAttribute("vendorId", vendorId);
        model.addAttribute("currencyId", currencyId);
        return "accountspayable/vendor-bills/select-references";
    }

    @PostMapping("/create-from-references")
    @PreAuthorize("hasAuthority('VENDOR-BILL_CREATE')")
    public String createFromReferences(@RequestParam(name = "selectedGrIds") List<Long> selectedGrIds,
                                       org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("selectedGrIds", selectedGrIds);
        return "redirect:/accounts-payable/vendor-bills/create";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('VENDOR-BILL_CREATE')")
    public String createForm(@RequestParam(required = false) Long vendorId,
                             @RequestParam(required = false) Long currencyId,
                             @ModelAttribute("selectedGrIds") List<Long> selectedGrIds,
                             Model model) {
        VendorBillCreateView view = getVendorBillCreateViewUseCase.execute(vendorId, currencyId);
        VendorBillFormView form = webMapper.toFormView(view);
        if (selectedGrIds != null && !selectedGrIds.isEmpty()) {
            form.request().setGrIds(selectedGrIds);
            form.request().setLines(prefillLines(selectedGrIds));
        }
        model.addAttribute("form", form);
        return "accountspayable/vendor-bills/form";
    }

    @GetMapping("/billable-grs/{grId}/lines")
    @ResponseBody
    @PreAuthorize("hasAuthority('VENDOR-BILL_CREATE')")
    public ResponseEntity<ApiResponse<List<BillableGrLineView>>> billableGrLines(@PathVariable Long grId) {
        List<BillableGrLineView> lines = findBillableGrLinesUseCase.execute(grId);
        return ResponseEntity.ok(ApiResponse.success(null, lines));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR-BILL_READ')")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("bill", webMapper.toDetailResponse(getVendorBillDetailUseCase.execute(id)));
        return "accountspayable/vendor-bills/detail";
    }

    @PostMapping
    @ResponseBody
    @PreAuthorize("hasAuthority('VENDOR-BILL_CREATE')")
    public ResponseEntity<ApiResponse<Void>> create(@Valid @RequestBody VendorBillSaveRequest request) {
        VendorBillSaveCommand command = webMapper.toCreateCommand(request);
        createVendorBillUseCase.execute(
                command.vendorId(),
                command.vendorInvoiceNumber(),
                command.billDate(),
                command.dueDate(),
                command.currencyId(),
                command.exchangeRate(),
                command.notes(),
                command.grIds(),
                command.lines()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(success("msg.success.create"));
    }

    @PutMapping("/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('VENDOR-BILL_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long id,
                                                    @Valid @RequestBody VendorBillSaveRequest request) {
        VendorBillSaveCommand command = webMapper.toUpdateCommand(id, request);
        updateVendorBillUseCase.execute(
                id,
                command.vendorId(),
                command.vendorInvoiceNumber(),
                command.billDate(),
                command.dueDate(),
                command.currencyId(),
                command.exchangeRate(),
                command.notes(),
                command.grIds(),
                command.lines()
        );
        return ResponseEntity.ok(success("msg.success.update"));
    }

    @PostMapping("/{id}/confirm")
    @ResponseBody
    @PreAuthorize("hasAuthority('VENDOR-BILL_CONFIRM')")
    public ResponseEntity<ApiResponse<VendorBillDetailResponse>> confirm(@PathVariable Long id) {
        confirmVendorBillUseCase.execute(id);
        VendorBillDetailResponse data = webMapper.toDetailResponse(getVendorBillDetailUseCase.execute(id));
        return ResponseEntity.ok(ApiResponse.success(message("msg.success.vb.confirmed"), data));
    }

    @PostMapping("/{id}/cancel")
    @ResponseBody
    @PreAuthorize("hasAuthority('VENDOR-BILL_CANCEL')")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long id) {
        cancelVendorBillUseCase.execute(id);
        return ResponseEntity.ok(success("msg.success.vb.cancelled"));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('VENDOR-BILL_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        deleteVendorBillUseCase.execute(id);
        return ResponseEntity.ok(success("msg.success.delete"));
    }

    private List<VendorBillLineRequest> prefillLines(List<Long> grIds) {
        return grIds.stream()
                .flatMap(grId -> findBillableGrLinesUseCase.execute(grId).stream())
                .map(this::toLineRequest)
                .toList();
    }

    private VendorBillLineRequest toLineRequest(BillableGrLineView line) {
        VendorBillLineRequest request = new VendorBillLineRequest();
        request.setGrLineId(line.grLineId());
        request.setProductId(line.productId());
        request.setProductName(line.productName());
        request.setDescription(line.productName());
        request.setQtyBilled(line.outstandingQty());
        request.setUomId(line.uomId());
        request.setUomName(line.uomName());
        request.setUnitPrice(line.unitPrice());
        request.setInventoryAmount(line.inventoryAmount());
        request.setTaxAmount(line.taxAmount());
        return request;
    }

    private ApiResponse<Void> success(String messageKey) {
        return ApiResponse.success(message(messageKey), null);
    }

    private String message(String messageKey) {
        return messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
    }
}
