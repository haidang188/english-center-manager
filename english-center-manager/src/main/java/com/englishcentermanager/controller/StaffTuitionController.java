package com.englishcentermanager.controller;

import com.englishcentermanager.dto.TuitionBatchForm;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.StudentTuition;
import com.englishcentermanager.entity.TuitionBatch;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.service.CourseClassService;
import com.englishcentermanager.service.StaffTuitionService;
import com.englishcentermanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
public class StaffTuitionController {
    private final StaffTuitionService staffTuitionService;
    private final CourseClassService courseClassService;
    private final UserService userService;

    public StaffTuitionController(StaffTuitionService staffTuitionService,
                                  CourseClassService courseClassService,
                                  UserService userService) {
        this.staffTuitionService = staffTuitionService;
        this.courseClassService = courseClassService;
        this.userService = userService;
    }

    @GetMapping("/staff/tuition-batches")
    public String tuitionBatches(@RequestParam(required = false) Long classId,
                                 @RequestParam(required = false) Long batchId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        List<TuitionBatch> batches = staffTuitionService.findBatches(classId);
        TuitionBatch selectedBatch = resolveSelectedBatch(batchId, batches);
        List<StudentTuition> studentTuitions = selectedBatch == null
                ? List.of()
                : staffTuitionService.findStudentTuitions(selectedBatch);

        model.addAttribute("classes", sortedClasses());
        model.addAttribute("batches", batches);
        model.addAttribute("selectedClassId", classId);
        model.addAttribute("selectedBatchId", selectedBatch == null ? null : selectedBatch.getId());
        model.addAttribute("selectedBatch", selectedBatch);
        model.addAttribute("studentTuitions", studentTuitions);
        model.addAttribute("tuitionBatchForm", defaultTuitionBatchForm(classId));
        model.addAttribute("staffTuitionService", staffTuitionService);

        return "staff/tuition-batches";
    }

    @PostMapping("/staff/tuition-batches")
    public String createTuitionBatch(@Valid @ModelAttribute("tuitionBatchForm") TuitionBatchForm form,
                                     BindingResult bindingResult,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        Long classId = form.getClassId();
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui long nhap day du thong tin dot hoc phi.");
            return classId == null ? "redirect:/staff/tuition-batches" : "redirect:/staff/tuition-batches?classId=" + classId;
        }

        try {
            TuitionBatch tuitionBatch = staffTuitionService.createBatch(form, currentUser(authentication));
            redirectAttributes.addFlashAttribute("successMessage", "Tao dot hoc phi va sinh hoc phi hoc vien thanh cong.");
            return "redirect:/staff/tuition-batches?classId="
                    + tuitionBatch.getCourseClass().getId()
                    + "&batchId="
                    + tuitionBatch.getId();
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return classId == null ? "redirect:/staff/tuition-batches" : "redirect:/staff/tuition-batches?classId=" + classId;
        }
    }

    private List<CourseClass> sortedClasses() {
        return courseClassService.findAll().stream()
                .sorted(Comparator.comparing(CourseClass::getClassName))
                .toList();
    }

    private TuitionBatch resolveSelectedBatch(Long batchId, List<TuitionBatch> batches) {
        if (batchId != null) {
            return staffTuitionService.findById(batchId).orElse(null);
        }

        return batches.isEmpty() ? null : batches.get(0);
    }

    private TuitionBatchForm defaultTuitionBatchForm(Long classId) {
        TuitionBatchForm form = new TuitionBatchForm();
        form.setClassId(classId);
        form.setDueDate(LocalDate.now().plusDays(30));
        return form;
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Khong xac dinh duoc tai khoan dang nhap");
        }

        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan dang nhap"));
    }
}
