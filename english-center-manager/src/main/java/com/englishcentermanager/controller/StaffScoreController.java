package com.englishcentermanager.controller;

import com.englishcentermanager.dto.ExamSessionForm;
import com.englishcentermanager.dto.StaffScoreBoard;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.ExamSession;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.service.CourseClassService;
import com.englishcentermanager.service.StaffScoreService;
import com.englishcentermanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
public class StaffScoreController {
    private final StaffScoreService staffScoreService;
    private final CourseClassService courseClassService;
    private final UserService userService;

    public StaffScoreController(StaffScoreService staffScoreService,
                                CourseClassService courseClassService,
                                UserService userService) {
        this.staffScoreService = staffScoreService;
        this.courseClassService = courseClassService;
        this.userService = userService;
    }

    @GetMapping("/staff/scores")
    public String scores(@RequestParam(required = false) Long classId,
                         @RequestParam(required = false) Long examSessionId,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        model.addAttribute("classes", sortedClasses());
        model.addAttribute("selectedClassId", classId);
        model.addAttribute("examSessionForm", defaultExamSessionForm());

        if (classId == null) {
            return "staff/scores";
        }

        CourseClass courseClass = courseClassService.findById(classId).orElse(null);
        if (courseClass == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong tim thay lop hoc.");
            return "redirect:/staff/scores";
        }

        List<ExamSession> sessions = staffScoreService.findSessions(courseClass);
        ExamSession selectedSession = resolveSelectedSession(courseClass, examSessionId);

        model.addAttribute("selectedClass", courseClass);
        model.addAttribute("sessions", sessions);
        model.addAttribute("selectedSessionId", selectedSession == null ? null : selectedSession.getId());

        if (selectedSession != null) {
            StaffScoreBoard scoreBoard = staffScoreService.buildScoreBoard(classId, selectedSession.getId());
            model.addAttribute("scoreBoard", scoreBoard);
        }

        return "staff/scores";
    }

    @PostMapping("/staff/scores/sessions")
    public String createSession(@RequestParam Long classId,
                                @Valid @ModelAttribute("examSessionForm") ExamSessionForm form,
                                BindingResult bindingResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui long nhap day du thong tin dot diem.");
            return "redirect:/staff/scores?classId=" + classId;
        }

        try {
            ExamSession examSession = staffScoreService.createSession(classId, form, currentUser(authentication));
            redirectAttributes.addFlashAttribute("successMessage", "Tao dot diem thanh cong.");
            return "redirect:/staff/scores?classId=" + classId + "&examSessionId=" + examSession.getId();
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/staff/scores?classId=" + classId;
        }
    }

    @PostMapping("/staff/scores/{classId}/{examSessionId}")
    public String saveScores(@PathVariable Long classId,
                             @PathVariable Long examSessionId,
                             @RequestParam Map<String, String> params,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            staffScoreService.saveScores(classId, examSessionId, params, currentUser(authentication));
            redirectAttributes.addFlashAttribute("successMessage", "Luu diem thanh cong.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/staff/scores?classId=" + classId + "&examSessionId=" + examSessionId;
    }

    private List<CourseClass> sortedClasses() {
        return courseClassService.findAll().stream()
                .sorted(Comparator.comparing(CourseClass::getClassName))
                .toList();
    }

    private ExamSession resolveSelectedSession(CourseClass courseClass, Long examSessionId) {
        if (examSessionId != null) {
            return staffScoreService.findSessions(courseClass).stream()
                    .filter(session -> session.getId().equals(examSessionId))
                    .findFirst()
                    .orElse(null);
        }

        return staffScoreService.findLatestSession(courseClass).orElse(null);
    }

    private ExamSessionForm defaultExamSessionForm() {
        ExamSessionForm form = new ExamSessionForm();
        form.setExamDate(LocalDate.now());
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
