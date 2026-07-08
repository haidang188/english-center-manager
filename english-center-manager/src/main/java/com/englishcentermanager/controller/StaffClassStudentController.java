package com.englishcentermanager.controller;

import com.englishcentermanager.entity.ClassStudent;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.Role;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.service.CourseClassService;
import com.englishcentermanager.service.RoleService;
import com.englishcentermanager.service.StaffClassStudentService;
import com.englishcentermanager.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
public class StaffClassStudentController {
    private final StaffClassStudentService staffClassStudentService;
    private final CourseClassService courseClassService;
    private final UserService userService;
    private final RoleService roleService;

    public StaffClassStudentController(StaffClassStudentService staffClassStudentService,
                                       CourseClassService courseClassService,
                                       UserService userService,
                                       RoleService roleService) {
        this.staffClassStudentService = staffClassStudentService;
        this.courseClassService = courseClassService;
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/staff/class-students")
    public String classStudentsShortcut(@RequestParam(required = false) Long classId) {
        if (classId == null) {
            return "redirect:/staff/classes";
        }

        return "redirect:/staff/classes/" + classId + "/students";
    }

    @GetMapping("/staff/classes/{classId}/students")
    public String listStudents(@PathVariable Long classId,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) enums.StudentClassStatus status,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        CourseClass courseClass = courseClassService.findById(classId).orElse(null);
        if (courseClass == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong tim thay lop hoc.");
            return "redirect:/staff/classes";
        }

        List<ClassStudent> classStudents = staffClassStudentService.searchInClass(classId, keyword, status);

        model.addAttribute("courseClass", courseClass);
        model.addAttribute("classStudents", classStudents);
        model.addAttribute("students", findActiveStudents());
        model.addAttribute("classes", courseClassService.findAll().stream()
                .filter(item -> !item.getId().equals(classId))
                .sorted(Comparator.comparing(CourseClass::getClassName))
                .toList());
        model.addAttribute("statuses", enums.StudentClassStatus.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("today", LocalDate.now());

        return "staff/class-students";
    }

    @PostMapping("/staff/classes/{classId}/students")
    public String addStudent(@PathVariable Long classId,
                             @RequestParam Long studentId,
                             @RequestParam(required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joinedAt,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            staffClassStudentService.addStudentToClass(classId, studentId, joinedAt, currentUser(authentication));
            redirectAttributes.addFlashAttribute("successMessage", "Them hoc vien vao lop thanh cong.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/staff/classes/" + classId + "/students";
    }

    @PostMapping("/staff/classes/{classId}/students/{classStudentId}/status")
    public String updateStatus(@PathVariable Long classId,
                               @PathVariable Long classStudentId,
                               @RequestParam enums.StudentClassStatus status,
                               @RequestParam(required = false) String note,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            staffClassStudentService.updateStatus(classStudentId, status, currentUser(authentication), note);
            redirectAttributes.addFlashAttribute("successMessage", "Cap nhat trang thai hoc vien thanh cong.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/staff/classes/" + classId + "/students";
    }

    @PostMapping("/staff/classes/{classId}/students/{classStudentId}/transfer")
    public String transferStudent(@PathVariable Long classId,
                                  @PathVariable Long classStudentId,
                                  @RequestParam Long targetClassId,
                                  @RequestParam(required = false) String note,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            staffClassStudentService.transferStudent(classStudentId, targetClassId, currentUser(authentication), note);
            redirectAttributes.addFlashAttribute("successMessage", "Chuyen hoc vien sang lop moi thanh cong.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/staff/classes/" + classId + "/students";
    }

    private List<User> findActiveStudents() {
        return roleService.findByName("STUDENT")
                .map(this::findActiveStudentsByRole)
                .orElse(List.of());
    }

    private List<User> findActiveStudentsByRole(Role role) {
        return userService.findByRole(role).stream()
                .filter(user -> user.getStatus() == enums.UserStatus.ACTIVE)
                .sorted(Comparator.comparing(User::getFullName))
                .toList();
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Khong xac dinh duoc tai khoan dang nhap");
        }

        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan dang nhap"));
    }
}
