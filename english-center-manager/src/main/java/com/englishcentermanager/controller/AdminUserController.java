package com.englishcentermanager.controller;

import com.englishcentermanager.dto.ResetPasswordForm;
import com.englishcentermanager.dto.UserForm;
import com.englishcentermanager.entity.Role;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.service.RoleService;
import com.englishcentermanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    private static final int DEFAULT_PAGE_SIZE = 8;

    private final UserService userService;
    private final RoleService roleService;

    public AdminUserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String listUsers(@RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "roleId", required = false) Long roleId,
                            @RequestParam(value = "status", required = false) enums.UserStatus status,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), DEFAULT_PAGE_SIZE, Sort.by("id").descending());
        Page<User> usersPage = userService.searchUsers(keyword, roleId, status, pageable);

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRoleId", roleId);
        model.addAttribute("selectedStatus", status);
        addCommonOptions(model);

        return "admin/users/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        UserForm userForm = new UserForm();
        userForm.setStatus(enums.UserStatus.ACTIVE);

        model.addAttribute("userForm", userForm);
        model.addAttribute("isEdit", false);
        addCommonOptions(model);

        return "admin/users/form";
    }

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("userForm") UserForm userForm,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        validateCreate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            addCommonOptions(model);
            return "admin/users/form";
        }

        Role role = roleService.findById(userForm.getRoleId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay vai tro"));

        User user = convertToEntity(userForm);
        user.setRole(role);

        userService.createByAdmin(user);

        redirectAttributes.addFlashAttribute("successMessage", "Thêm tài khoản thành công.");
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan"));

        UserForm userForm = convertToForm(user);

        model.addAttribute("userForm", userForm);
        model.addAttribute("isEdit", true);
        addCommonOptions(model);

        return "admin/users/form";
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("userForm") UserForm userForm,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        validateUpdate(id, userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            addCommonOptions(model);
            return "admin/users/form";
        }

        Role role = roleService.findById(userForm.getRoleId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay vai tro"));

        User user = convertToEntity(userForm);
        user.setRole(role);

        userService.updateByAdmin(id, user);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài khoản thành công.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/lock")
    public String lockUser(@PathVariable Long id,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        if (isCurrentUser(id, authentication)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không thể khóa tài khoản đang đăng nhập.");
            return "redirect:/admin/users";
        }

        userService.lockUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "Khóa tài khoản thành công.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/unlock")
    public String unlockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.unlockUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "Mở khóa tài khoản thành công.");
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/reset-password")
    public String showResetPasswordForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan"));

        model.addAttribute("user", user);
        model.addAttribute("resetPasswordForm", new ResetPasswordForm());
        return "admin/users/reset-password";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id,
                                @Valid @ModelAttribute("resetPasswordForm") ResetPasswordForm resetPasswordForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan"));

        if (!resetPasswordForm.getNewPassword().equals(resetPasswordForm.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Mật khẩu xác nhận không khớp");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "admin/users/reset-password";
        }

        userService.resetPassword(id, resetPasswordForm.getNewPassword());
        redirectAttributes.addFlashAttribute("successMessage", "Đặt lại mật khẩu thành công.");
        return "redirect:/admin/users";
    }

    private void validateCreate(UserForm userForm, BindingResult bindingResult) {
        if (userForm.getPassword() == null || userForm.getPassword().trim().isEmpty()) {
            bindingResult.rejectValue("password", "error.password", "Vui lòng nhập mật khẩu");
        }
        if (userService.existsByEmail(userForm.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "Email đã được sử dụng");
        }
        if (hasText(userForm.getIdentityNumber()) && userService.existsByIdentityNumber(userForm.getIdentityNumber())) {
            bindingResult.rejectValue("identityNumber", "error.identityNumber", "CCCD/CMND đã được sử dụng");
        }
    }

    private void validateUpdate(Long id, UserForm userForm, BindingResult bindingResult) {
        if (userService.existsByEmailAndIdNot(userForm.getEmail(), id)) {
            bindingResult.rejectValue("email", "error.email", "Email đã được sử dụng");
        }
        if (hasText(userForm.getIdentityNumber())
                && userService.existsByIdentityNumberAndIdNot(userForm.getIdentityNumber(), id)) {
            bindingResult.rejectValue("identityNumber", "error.identityNumber", "CCCD/CMND đã được sử dụng");
        }
    }

    private boolean isCurrentUser(Long id, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        return userService.findByEmail(authentication.getName())
                .map(currentUser -> currentUser.getId().equals(id))
                .orElse(false);
    }

    private void addCommonOptions(Model model) {
        model.addAttribute("roles", roleService.findAll());
        model.addAttribute("statuses", enums.UserStatus.values());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private User convertToEntity(UserForm userForm) {
        User user = new User();
        user.setId(userForm.getId());
        user.setFullName(userForm.getFullName());
        user.setEmail(userForm.getEmail());
        user.setPassword(userForm.getPassword());
        user.setPhoneNumber(userForm.getPhoneNumber());
        user.setDateOfBirth(userForm.getDateOfBirth());
        user.setAddress(userForm.getAddress());
        user.setIdentityNumber(userForm.getIdentityNumber());
        user.setStatus(userForm.getStatus());
        return user;
    }

    private UserForm convertToForm(User user) {
        UserForm userForm = new UserForm();
        userForm.setId(user.getId());
        userForm.setFullName(user.getFullName());
        userForm.setEmail(user.getEmail());
        userForm.setPhoneNumber(user.getPhoneNumber());
        userForm.setDateOfBirth(user.getDateOfBirth());
        userForm.setAddress(user.getAddress());
        userForm.setIdentityNumber(user.getIdentityNumber());
        userForm.setRoleId(user.getRole().getId());
        userForm.setStatus(user.getStatus());
        return userForm;
    }
}
