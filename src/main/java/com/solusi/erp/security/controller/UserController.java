package com.solusi.erp.security.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.security.dto.UserRequest;
import com.solusi.erp.security.dto.UserResponse;
import com.solusi.erp.security.service.RoleService;
import com.solusi.erp.security.service.UserService;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/security/users")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('USERS_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable,
            Model model) {
        model.addAttribute("page", userService.findAll(keyword, pageable));
        return "security/users/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('USERS_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("userRequest", new UserRequest());
        populateSelectOptions(model);
        return "security/users/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('USERS_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserRequest request) {
        UserResponse data = userService.create(request);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('USERS_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        var viewDto = userService.getUserEditView(id);
        model.addAttribute("userRequest", viewDto.getRequest());
        model.addAttribute("userUIForm", viewDto.getUi());
        model.addAttribute("auditInfo", viewDto.getAudit());
        populateSelectOptions(model);
        return "security/users/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('USERS_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        UserResponse data = userService.update(id, request);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @PostMapping("/toggle/{id}")
    @PreAuthorize("hasAuthority('USERS_UPDATE')")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.toggleStatus(id);
        String message = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/security/users";
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USERS_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    private void populateSelectOptions(Model model) {
        model.addAttribute("roles", roleService.findAll());
    }
}
