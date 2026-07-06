package com.englishcentermanager.controller;

import com.englishcentermanager.dto.UserForm;
import com.englishcentermanager.entity.Role;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.service.RoleService;
import com.englishcentermanager.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService userService;
    private final RoleService roleService;

    public AdminUserController(UserService userService, RoleService roleService){
        this.userService = userService;
        this.roleService = roleService;
    }
    @GetMapping
    public String listUsers(@RequestParam(value = "keyword", required = false) String keyword, Model model){
        if(keyword != null && !keyword.trim().isEmpty()){
            model.addAttribute("users", userService.searchByKeyword(keyword));
            model.addAttribute("keyword", keyword);
        }{
            model.addAttribute("users", userService.findAll());
        }
        return "admin/users/list";
    }
    @GetMapping("/create")
    public String showCreateForm(Model model){
        UserForm userForm = new UserForm();
        userForm.setStatus(enums.UserStatus.ACTIVE);
    }
}
