package com.englishcentermanager.controller;

import com.englishcentermanager.dto.StaffScoreBoard;
import com.englishcentermanager.entity.ClassStudent;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.ExamSession;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.repository.ClassStudentRepository;
import com.englishcentermanager.repository.CourseClassRepository;
import com.englishcentermanager.repository.ScoreEntryRepository;
import com.englishcentermanager.service.StaffScoreService;
import com.englishcentermanager.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
public class TeacherDashboardController {
    private final UserService userService;
    private final CourseClassRepository courseClassRepository;
    private final ClassStudentRepository classStudentRepository;
    private final ScoreEntryRepository scoreEntryRepository;
    private final StaffScoreService staffScoreService;

    public TeacherDashboardController(UserService userService,
                                      CourseClassRepository courseClassRepository,
                                      ClassStudentRepository classStudentRepository,
                                      ScoreEntryRepository scoreEntryRepository,
                                      StaffScoreService staffScoreService) {
        this.userService = userService;
        this.courseClassRepository = courseClassRepository;
        this.classStudentRepository = classStudentRepository;
        this.scoreEntryRepository = scoreEntryRepository;
        this.staffScoreService = staffScoreService;
    }

    @GetMapping({"/teacher", "/teacher/dashboard"})
    public String dashboard(Authentication authentication, Model model) {
        User teacher = currentUser(authentication);
        List<CourseClass> classes = teacherClasses(teacher);
        long studentCount = classes.stream()
                .mapToLong(classStudentRepository::countByCourseClass)
                .sum();

        model.addAttribute("teacher", teacher);
        model.addAttribute("classes", classes);
        model.addAttribute("classCount", classes.size());
        model.addAttribute("studentCount", studentCount);
        return "teacher/dashboard";
    }

    @GetMapping("/teacher/classes")
    public String classes(@RequestParam(required = false) String keyword,
                          Authentication authentication,
                          Model model) {
        User teacher = currentUser(authentication);
        List<CourseClass> classes = teacherClasses(teacher);
        if (hasText(keyword)) {
            String search = keyword.trim().toLowerCase();
            classes = classes.stream()
                    .filter(courseClass -> contains(courseClass.getClassName(), search)
                            || contains(courseClass.getClassCode(), search)
                            || contains(courseClass.getCourse().getCourseName(), search))
                    .toList();
        }

        model.addAttribute("classes", classes);
        model.addAttribute("keyword", keyword);
        model.addAttribute("classStudentRepository", classStudentRepository);
        return "teacher/classes";
    }

    @GetMapping("/teacher/classes/{classId}/students")
    public String students(@PathVariable Long classId,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(required = false) enums.StudentClassStatus status,
                           Authentication authentication,
                           Model model) {
        CourseClass courseClass = teacherClass(classId, authentication);
        List<ClassStudent> students = classStudentRepository.searchInClass(classId, normalize(keyword), status);

        model.addAttribute("courseClass", courseClass);
        model.addAttribute("students", students);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("studentStatuses", enums.StudentClassStatus.values());
        return "teacher/students";
    }

    @GetMapping("/teacher/classes/{classId}/students/{classStudentId}")
    public String studentDetail(@PathVariable Long classId,
                                @PathVariable Long classStudentId,
                                Authentication authentication,
                                Model model) {
        CourseClass courseClass = teacherClass(classId, authentication);
        ClassStudent classStudent = classStudentRepository.findById(classStudentId)
                .filter(item -> item.getCourseClass().getId().equals(classId))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên trong lớp."));

        model.addAttribute("courseClass", courseClass);
        model.addAttribute("classStudent", classStudent);
        model.addAttribute("scoreEntries", scoreEntryRepository.findByClassStudentForDisplay(classStudent));
        return "teacher/student-detail";
    }

    @GetMapping("/teacher/scores")
    public String scores(@RequestParam(required = false) Long classId,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        User teacher = currentUser(authentication);
        List<CourseClass> classes = teacherClasses(teacher);
        model.addAttribute("classes", classes);
        model.addAttribute("selectedClassId", classId);

        if (classId == null) {
            return "teacher/scores";
        }

        CourseClass courseClass = classes.stream()
                .filter(item -> item.getId().equals(classId))
                .findFirst()
                .orElse(null);

        if (courseClass == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không phụ trách lớp học này.");
            return "redirect:/teacher/scores";
        }

        ExamSession examSession = staffScoreService.findOrCreateDefaultSession(classId, teacher);
        StaffScoreBoard scoreBoard = staffScoreService.buildScoreBoard(classId, examSession.getId());

        model.addAttribute("selectedClass", courseClass);
        model.addAttribute("scoreBoard", scoreBoard);
        return "teacher/scores";
    }

    @GetMapping("/teacher/scores/{classId}/students/{classStudentId}")
    public String scoreDetail(@PathVariable Long classId,
                              @PathVariable Long classStudentId,
                              Authentication authentication,
                              Model model) {
        User teacher = currentUser(authentication);
        CourseClass courseClass = teacherClass(classId, authentication);
        ClassStudent classStudent = classStudentRepository.findById(classStudentId)
                .filter(item -> item.getCourseClass().getId().equals(classId))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên trong lớp."));
        ExamSession examSession = staffScoreService.findOrCreateDefaultSession(classId, teacher);
        StaffScoreBoard scoreBoard = staffScoreService.buildScoreBoard(classId, examSession.getId());

        model.addAttribute("courseClass", courseClass);
        model.addAttribute("classStudent", classStudent);
        model.addAttribute("scoreBoard", scoreBoard);
        return "teacher/score-detail";
    }

    @PostMapping("/teacher/scores/{classId}/students/{classStudentId}")
    public String saveStudentScores(@PathVariable Long classId,
                                    @PathVariable Long classStudentId,
                                    @RequestParam Map<String, String> params,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        User teacher = currentUser(authentication);
        teacherClass(classId, authentication);
        ExamSession examSession = staffScoreService.findOrCreateDefaultSession(classId, teacher);

        try {
            staffScoreService.saveScores(classId, examSession.getId(), params, teacher);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu điểm thành công.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/teacher/scores/" + classId + "/students/" + classStudentId;
        }

        return "redirect:/teacher/scores?classId=" + classId;
    }

    private CourseClass teacherClass(Long classId, Authentication authentication) {
        User teacher = currentUser(authentication);
        return courseClassRepository.findById(classId)
                .filter(courseClass -> courseClass.getTeacher() != null
                        && courseClass.getTeacher().getId().equals(teacher.getId()))
                .orElseThrow(() -> new RuntimeException("Bạn không phụ trách lớp học này."));
    }

    private List<CourseClass> teacherClasses(User teacher) {
        return courseClassRepository.findByTeacher(teacher).stream()
                .sorted(Comparator.comparing(CourseClass::getClassName))
                .toList();
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Không xác định được tài khoản đăng nhập.");
        }

        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản đăng nhập."));
    }

    private String normalize(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean contains(String source, String search) {
        return source != null && source.toLowerCase().contains(search);
    }
}
