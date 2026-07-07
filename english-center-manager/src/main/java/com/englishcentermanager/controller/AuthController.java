package com.englishcentermanager.controller;

import com.englishcentermanager.dto.RegisterForm;
import com.englishcentermanager.entity.Role;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.service.RoleService;
import com.englishcentermanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    private final UserService userService;
    private final RoleService roleService;

    public AuthController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping({"/", "/login"})
    public String showLoginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("registerForm") RegisterForm registerForm,
            BindingResult bindingResult
    ) {
        if (userService.existsByEmail(registerForm.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "Email da duoc su dung");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        Role studentRole = roleService.findByName("STUDENT")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("STUDENT");
                    role.setDescription("Hoc vien");
                    return roleService.save(role);
                });

        User user = new User();
        user.setFullName(registerForm.getFullName());
        user.setEmail(registerForm.getEmail());
        user.setPassword(registerForm.getPassword());
        user.setPhoneNumber(registerForm.getPhoneNumber());
        user.setRole(studentRole);

        userService.register(user);

        return "redirect:/login?registered=true";
    }

    @GetMapping("/home")
    public String home() {
        return "auth/home";
    }
}
