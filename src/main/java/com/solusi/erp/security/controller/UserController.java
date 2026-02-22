package com.solusi.erp.security.controller;

import com.solusi.erp.security.dto.UserRequest;
import com.solusi.erp.security.dto.UserResponse;
import com.solusi.erp.security.service.RoleService;
import com.solusi.erp.security.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/security/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('USERS_READ')")
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
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
            redirectAttributes.addFlashAttribute("successMessage", "Pengguna berhasil dibuat");
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
            redirectAttributes.addFlashAttribute("successMessage", "Data pengguna berhasil diperbarui");
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
            redirectAttributes.addFlashAttribute("successMessage", "Status pengguna berhasil diubah");
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
            redirectAttributes.addFlashAttribute("successMessage", "Pengguna berhasil dihapus");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/security/users";
    }
}
