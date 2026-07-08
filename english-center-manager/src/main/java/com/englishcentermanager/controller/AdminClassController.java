package com.englishcentermanager.controller;

import com.englishcentermanager.dto.CourseClassForm;
import com.englishcentermanager.dto.StaffScoreBoard;
import com.englishcentermanager.entity.Course;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.ExamSession;
import com.englishcentermanager.entity.Role;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.service.CourseClassService;
import com.englishcentermanager.service.CourseService;
import com.englishcentermanager.service.RoleService;
import com.englishcentermanager.service.StaffScoreService;
import com.englishcentermanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/classes")
public class AdminClassController {
    private static final int PAGE_SIZE = 8;

    private final CourseClassService courseClassService;
    private final CourseService courseService;
    private final UserService userService;
    private final RoleService roleService;
    private final StaffScoreService staffScoreService;

    public AdminClassController(CourseClassService courseClassService,
                                CourseService courseService,
                                UserService userService,
                                RoleService roleService,
                                StaffScoreService staffScoreService) {
        this.courseClassService = courseClassService;
        this.courseService = courseService;
        this.userService = userService;
        this.roleService = roleService;
        this.staffScoreService = staffScoreService;
    }

    @GetMapping
    public String listClasses(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Long courseId,
                              @RequestParam(required = false) enums.ClassStatus status,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {
        List<CourseClass> classes = courseClassService.findAll();

        if (hasText(keyword)) {
            String searchValue = keyword.trim().toLowerCase();
            classes = classes.stream()
                    .filter(courseClass -> courseClass.getClassCode().toLowerCase().contains(searchValue)
                            || courseClass.getClassName().toLowerCase().contains(searchValue)
                            || courseClass.getTeacher().getFullName().toLowerCase().contains(searchValue))
                    .toList();
        }

        if (courseId != null) {
            classes = classes.stream()
                    .filter(courseClass -> courseClass.getCourse().getId().equals(courseId))
                    .toList();
        }

        if (status != null) {
            classes = classes.stream()
                    .filter(courseClass -> courseClass.getStatus() == status)
                    .toList();
        }

        classes = classes.stream()
                .sorted(Comparator.comparing(CourseClass::getId).reversed())
                .toList();

        Page<CourseClass> classesPage = toPage(classes, page);

        model.addAttribute("classesPage", classesPage);
        model.addAttribute("classes", classesPage.getContent());
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("statuses", enums.ClassStatus.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedStatus", status);

        return "admin/classes/list";
    }

    @GetMapping("/{id}")
    public String classDetail(@PathVariable Long id,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        CourseClass courseClass = courseClassService.findById(id).orElse(null);

        if (courseClass == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy lớp học.");
            return "redirect:/admin/classes";
        }

        ExamSession latestSession = staffScoreService.findLatestSession(courseClass).orElse(null);
        StaffScoreBoard scoreBoard = null;
        if (latestSession != null) {
            scoreBoard = staffScoreService.buildScoreBoard(id, latestSession.getId());
        }

        model.addAttribute("courseClass", courseClass);
        model.addAttribute("latestSession", latestSession);
        model.addAttribute("scoreBoard", scoreBoard);

        return "admin/classes/detail";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        CourseClassForm classForm = new CourseClassForm();
        classForm.setStatus(enums.ClassStatus.PLANNED);

        model.addAttribute("classForm", classForm);
        model.addAttribute("isEdit", false);
        addFormOptions(model);

        return "admin/classes/form";
    }

    @PostMapping("/create")
    public String createClass(@Valid @ModelAttribute("classForm") CourseClassForm classForm,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        validateClassDates(classForm, bindingResult);

        if (courseClassService.existsByClassCode(classForm.getClassCode())) {
            bindingResult.rejectValue("classCode", "duplicate", "Mã lớp đã tồn tại");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            addFormOptions(model);
            return "admin/classes/form";
        }

        courseClassService.save(toEntity(classForm));
        redirectAttributes.addFlashAttribute("successMessage", "Thêm lớp học thành công.");

        return "redirect:/admin/classes";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        CourseClass courseClass = courseClassService.findById(id).orElse(null);

        if (courseClass == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy lớp học.");
            return "redirect:/admin/classes";
        }

        model.addAttribute("classForm", toForm(courseClass));
        model.addAttribute("isEdit", true);
        addFormOptions(model);

        return "admin/classes/form";
    }

    @PostMapping("/{id}/edit")
    public String updateClass(@PathVariable Long id,
                              @Valid @ModelAttribute("classForm") CourseClassForm classForm,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (courseClassService.findById(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy lớp học.");
            return "redirect:/admin/classes";
        }

        validateClassDates(classForm, bindingResult);

        if (courseClassService.existsByClassCodeAndIdNot(classForm.getClassCode(), id)) {
            bindingResult.rejectValue("classCode", "duplicate", "Mã lớp đã tồn tại");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            addFormOptions(model);
            return "admin/classes/form";
        }

        courseClassService.update(id, toEntity(classForm));
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật lớp học thành công.");

        return "redirect:/admin/classes";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam enums.ClassStatus status,
                               RedirectAttributes redirectAttributes) {
        courseClassService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái lớp học thành công.");

        return "redirect:/admin/classes";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("courses", courseService.findAllActive());
        model.addAttribute("teachers", findTeachers());
        model.addAttribute("statuses", enums.ClassStatus.values());
    }

    private List<User> findTeachers() {
        return roleService.findByName("TEACHER")
                .map(userService::findByRole)
                .orElse(List.of());
    }

    private CourseClass toEntity(CourseClassForm form) {
        Course course = courseService.findById(form.getCourseId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));
        User teacher = userService.findById(form.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên"));

        CourseClass courseClass = new CourseClass();
        courseClass.setClassCode(form.getClassCode().trim());
        courseClass.setClassName(form.getClassName().trim());
        courseClass.setCourse(course);
        courseClass.setTeacher(teacher);
        courseClass.setStartDate(form.getStartDate());
        courseClass.setEndDate(form.getEndDate());
        courseClass.setStatus(form.getStatus());

        return courseClass;
    }

    private CourseClassForm toForm(CourseClass courseClass) {
        CourseClassForm form = new CourseClassForm();
        form.setId(courseClass.getId());
        form.setClassCode(courseClass.getClassCode());
        form.setClassName(courseClass.getClassName());
        form.setCourseId(courseClass.getCourse().getId());
        form.setTeacherId(courseClass.getTeacher().getId());
        form.setStartDate(courseClass.getStartDate());
        form.setEndDate(courseClass.getEndDate());
        form.setStatus(courseClass.getStatus());

        return form;
    }

    private void validateClassDates(CourseClassForm form, BindingResult bindingResult) {
        if (form.getStartDate() != null
                && form.getEndDate() != null
                && form.getEndDate().isBefore(form.getStartDate())) {
            bindingResult.rejectValue("endDate", "invalid", "Ngay ket thuc phai sau ngay bat dau");
        }
    }

    private Page<CourseClass> toPage(List<CourseClass> classes, int page) {
        int safePage = Math.max(page, 0);
        int start = Math.min(safePage * PAGE_SIZE, classes.size());
        int end = Math.min(start + PAGE_SIZE, classes.size());

        return new PageImpl<>(
                classes.subList(start, end),
                PageRequest.of(safePage, PAGE_SIZE),
                classes.size()
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
