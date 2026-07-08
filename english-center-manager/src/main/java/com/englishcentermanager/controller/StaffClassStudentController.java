package com.englishcentermanager.controller;

import com.englishcentermanager.dto.AddStudentForm;
import com.englishcentermanager.dto.TransferStudentForm;
import com.englishcentermanager.dto.UpdateStudentStatusForm;
import com.englishcentermanager.entity.ClassStudent;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.service.StaffClassStudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffClassStudentController {
    private final StaffClassStudentService staffClassStudentService;

    @GetMapping({"", "/"})
    public String dashboard() {
        return "redirect:/staff/classes";
    }

    @GetMapping("/classes")
    public String viewClasses(@RequestParam(required = false) String keyword, Model model) {
        List<CourseClass> classes = staffClassStudentService.getAllClasses(keyword);
        model.addAttribute("keyword", keyword);
        model.addAttribute("classes", classes);

        return "staff/classes";
    }

    @GetMapping("/classes/{classId}/students")
    public String viewStudents(@PathVariable Long classId,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) enums.StudentClassStatus status,
                               @PageableDefault(size = 10) Pageable pageable,
                               Model model) {
        Page<ClassStudent> studentsPage = staffClassStudentService.getStudentsByClass(classId, keyword, status, pageable);

        model.addAttribute("courseClass", staffClassStudentService.getClassById(classId));
        model.addAttribute("students", studentsPage.getContent());
        model.addAttribute("studentsPage", studentsPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("studentStatuses", enums.StudentClassStatus.values());


        return "staff/class-students";

    }

    @GetMapping("/classes/{classId}/students/add")
    public String showAddStudentForm(@PathVariable Long classId,
                                     @RequestParam(required = false) String keyword,
                                     @PageableDefault(size = 10) Pageable pageable,
                                     Model model) {
        Page<User> studentsPage = staffClassStudentService.getStudents(keyword, pageable);

        model.addAttribute("classId", classId);
        model.addAttribute("courseClass", staffClassStudentService.getClassById(classId));
        model.addAttribute("students", studentsPage.getContent());
        model.addAttribute("studentsPage", studentsPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("addStudentForm", new AddStudentForm());

        return "staff/add-student";
    }

    @PostMapping("/classes/{classId}/students/add")
    public String addStudent(@PathVariable Long classId,
                             @RequestParam Long studentId,
                             RedirectAttributes redirectAttributes) {
        try {
            staffClassStudentService.addStudentToClass(classId, studentId);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm học viên thành công");
            return redirectToStudents(classId);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/staff/classes/" + classId + "/students/add";
        }
    }

    @GetMapping("/class-students/{id}/status")
    public String showStatusForm(@PathVariable Long id, Model model) {
        ClassStudent classStudent = staffClassStudentService.getClassStudentById(id);
        UpdateStudentStatusForm form = new UpdateStudentStatusForm();
        form.setClassStudentId(id);
        form.setStatus(classStudent.getStatus());

        populateStatusModel(model, classStudent, form);
        return "staff/update-student-status";
    }

    @PostMapping("/class-students/{id}/status")
    public String updateStatusById(@PathVariable Long id,
                                   @Valid @ModelAttribute("updateStudentStatusForm") UpdateStudentStatusForm form,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        ClassStudent classStudent = staffClassStudentService.getClassStudentById(id);
        form.setClassStudentId(id);

        if (bindingResult.hasErrors()) {
            populateStatusModel(model, classStudent, form);
            return "staff/update-student-status";
        }

        try {
            staffClassStudentService.updateStudentStatus(id, form.getStatus(), form.getNote());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return redirectToStudents(classStudent.getCourseClass().getId());
    }

    @PostMapping("/class-students/status")
    public String updateStatus(@Valid @ModelAttribute UpdateStudentStatusForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        ClassStudent classStudent = form.getClassStudentId() == null
                ? null
                : staffClassStudentService.getClassStudentById(form.getClassStudentId());

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu không hợp lệ");
            return classStudent == null ? "redirect:/staff/classes" : redirectToStudents(classStudent.getCourseClass().getId());
        }

        try {
            staffClassStudentService.updateStudentStatus(form.getClassStudentId(), form.getStatus(), form.getNote());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return classStudent == null ? "redirect:/staff/classes" : redirectToStudents(classStudent.getCourseClass().getId());
    }

    @GetMapping("/class-students/transfer")
    public String showTransferForm(@RequestParam Long studentId, @RequestParam Long fromClassId, Model model) {
        TransferStudentForm form = new TransferStudentForm();
        form.setStudentId(studentId);
        form.setFromClassId(fromClassId);

        populateTransferModel(model, studentId, fromClassId, form);
        return "staff/transfer-student";

    }

    @PostMapping("/class-students/transfer")
    public String transferStudent(@Valid @ModelAttribute("transferStudentForm") TransferStudentForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateTransferModel(model, form.getStudentId(), form.getFromClassId(), form);
            return "staff/transfer-student";
        }

        try {
            staffClassStudentService.transferStudent(
                    form.getStudentId(),
                    form.getFromClassId(),
                    form.getToClassId(),
                    form.getNote()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Chuyển lớp thành công");
            return redirectToStudents(form.getToClassId());
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/staff/class-students/transfer?studentId="
                    + form.getStudentId()
                    + "&fromClassId="
                    + form.getFromClassId();
        }
    }

    private void populateStatusModel(Model model, ClassStudent classStudent, UpdateStudentStatusForm form) {
        model.addAttribute("classStudent", classStudent);
        model.addAttribute("updateStudentStatusForm", form);
        model.addAttribute("studentStatuses", enums.StudentClassStatus.values());
    }

    private void populateTransferModel(Model model, Long studentId, Long fromClassId, TransferStudentForm form) {
        model.addAttribute("student", staffClassStudentService.getStudentById(studentId));
        model.addAttribute("fromClass", staffClassStudentService.getClassById(fromClassId));
        model.addAttribute("fromClassId", fromClassId);
        model.addAttribute("classes", staffClassStudentService.getAllClasses());
        model.addAttribute("transferStudentForm", form);
    }

    private String redirectToStudents(Long classId) {
        return "redirect:/staff/classes/" + classId + "/students";
    }
}
