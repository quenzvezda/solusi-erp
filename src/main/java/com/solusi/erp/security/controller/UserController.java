package com.solusi.erp.security.controller;

import com.solusi.erp.security.dto.UserRequest;

import com.solusi.erp.security.service.RoleService;
import com.solusi.erp.security.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Pageable;

@Controller
@RequestMapping("/security/users")
@RequiredArgsConstructor
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
        model.addAttribute("keyword", keyword);
        return "security/users/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('USERS_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("userRequest", new UserRequest());
        model.addAttribute("roles", roleService.findAll());
        return "security/users/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('USERS_CREATE')")
    public String create(@Valid @ModelAttribute("userRequest") UserRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleService.findAll());
            return "security/users/form";
        }

        try {
            userService.create(request);
            String message = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/security/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", roleService.findAll());
            return "security/users/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('USERS_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("userRequest", userService.getEditData(id));
        model.addAttribute("roles", roleService.findAll());
        return "security/users/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('USERS_UPDATE')")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("userRequest") UserRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleService.findAll());
            return "security/users/form";
        }

        try {
            userService.update(id, request);
            String message = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/security/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", roleService.findAll());
            return "security/users/form";
        }
    }

    @PostMapping("/toggle/{id}")
    @PreAuthorize("hasAuthority('USERS_UPDATE')")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleStatus(id);
            String message = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/security/users";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('USERS_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.delete(id);
            String message = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/security/users";
    }
}
