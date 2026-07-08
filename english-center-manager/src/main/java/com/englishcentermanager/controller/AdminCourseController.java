package com.englishcentermanager.controller;

import com.englishcentermanager.dto.CourseForm;
import com.englishcentermanager.dto.CourseScoreComponentForm;
import com.englishcentermanager.entity.Course;
import com.englishcentermanager.entity.CourseScoreComponent;
import com.englishcentermanager.entity.CourseType;
import com.englishcentermanager.service.CourseScoreComponentService;
import com.englishcentermanager.service.CourseService;
import com.englishcentermanager.service.CourseTypeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/courses")
public class AdminCourseController {
    private final CourseService courseService;
    private final CourseTypeService courseTypeService;
    private final CourseScoreComponentService courseScoreComponentService;

    public AdminCourseController(
            CourseService courseService,
            CourseTypeService courseTypeService,
            CourseScoreComponentService courseScoreComponentService
    ) {
        this.courseService = courseService;
        this.courseTypeService = courseTypeService;
        this.courseScoreComponentService = courseScoreComponentService;
    }

    @GetMapping
    public String listCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long courseTypeId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        List<Course> courses = courseService.findAll();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchValue = keyword.trim().toLowerCase();
            courses = courses.stream()
                    .filter(course ->
                            course.getCourseCode().toLowerCase().contains(searchValue)
                                    || course.getCourseName().toLowerCase().contains(searchValue)
                    )
                    .toList();
        }

        if (courseTypeId != null) {
            courses = courses.stream()
                    .filter(course -> course.getCourseType().getId().equals(courseTypeId))
                    .toList();
        }

        if (active != null) {
            courses = courses.stream()
                    .filter(course -> course.getActive().equals(active))
                    .toList();
        }

        courses = courses.stream()
                .sorted(Comparator.comparing(Course::getId).reversed())
                .toList();

        int pageSize = 8;
        int start = Math.min(page * pageSize, courses.size());
        int end = Math.min(start + pageSize, courses.size());
        Page<Course> coursePage = new PageImpl<>(
                courses.subList(start, end),
                PageRequest.of(page, pageSize),
                courses.size()
        );

        model.addAttribute("coursePage", coursePage);
        model.addAttribute("courseTypes", courseTypeService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("courseTypeId", courseTypeId);
        model.addAttribute("active", active);

        return "admin/courses/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("courseForm", new CourseForm());
        model.addAttribute("courseTypes", courseTypeService.findAllActive());
        model.addAttribute("title", "Thêm khóa học");
        model.addAttribute("actionUrl", "/admin/courses/create");

        return "admin/courses/form";
    }

    @PostMapping("/create")
    public String createCourse(
            @Valid @ModelAttribute("courseForm") CourseForm courseForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (courseService.existsByCourseCode(courseForm.getCourseCode())) {
            bindingResult.rejectValue("courseCode", "duplicate", "Mã khóa học đã tồn tại");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("courseTypes", courseTypeService.findAllActive());
            model.addAttribute("title", "Thêm khóa học");
            model.addAttribute("actionUrl", "/admin/courses/create");
            return "admin/courses/form";
        }

        Course course = buildCourseFromForm(courseForm);
        courseService.save(course);

        redirectAttributes.addFlashAttribute("successMessage", "Thêm khóa học thành công");
        return "redirect:/admin/courses";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Course course = courseService.findById(id).orElse(null);

        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khóa học");
            return "redirect:/admin/courses";
        }

        model.addAttribute("courseForm", buildFormFromCourse(course));
        model.addAttribute("courseTypes", courseTypeService.findAll());
        model.addAttribute("title", "Chỉnh sửa khóa học");
        model.addAttribute("actionUrl", "/admin/courses/" + id + "/edit");

        return "admin/courses/form";
    }

    @PostMapping("/{id}/edit")
    public String updateCourse(
            @PathVariable Long id,
            @Valid @ModelAttribute("courseForm") CourseForm courseForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Course existingCourse = courseService.findById(id).orElse(null);

        if (existingCourse == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khóa học");
            return "redirect:/admin/courses";
        }

        if (!existingCourse.getCourseCode().equalsIgnoreCase(courseForm.getCourseCode())
                && courseService.existsByCourseCode(courseForm.getCourseCode())) {
            bindingResult.rejectValue("courseCode", "duplicate", "Mã khóa học đã tồn tại");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("courseTypes", courseTypeService.findAll());
            model.addAttribute("title", "Chỉnh sửa khóa học");
            model.addAttribute("actionUrl", "/admin/courses/" + id + "/edit");
            return "admin/courses/form";
        }

        Course course = buildCourseFromForm(courseForm);
        courseService.update(id, course);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật khóa học thành công");
        return "redirect:/admin/courses";
    }

    @PostMapping("/{id}/toggle")
    public String toggleCourseStatus(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        Course course = courseService.findById(id).orElse(null);

        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khóa học");
            return "redirect:/admin/courses";
        }

        if (Boolean.TRUE.equals(course.getActive())) {
            courseService.deactivate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã ngưng sử dụng khóa học");
        } else {
            courseService.activate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã kích hoạt khóa học");
        }

        return "redirect:/admin/courses";
    }

    @GetMapping("/{courseId}/score-components")
    public String listScoreComponents(
            @PathVariable Long courseId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Course course = courseService.findById(courseId).orElse(null);

        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khóa học");
            return "redirect:/admin/courses";
        }

        List<CourseScoreComponent> components =
                courseScoreComponentService.findByCourseOrderByDisplayOrder(course);

        BigDecimal totalWeight = components.stream()
                .filter(component -> Boolean.TRUE.equals(component.getCalculated()))
                .map(CourseScoreComponent::getWeightPercent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("course", course);
        model.addAttribute("components", components);
        model.addAttribute("totalWeight", totalWeight);
        model.addAttribute("totalWeightComplete", totalWeight.compareTo(BigDecimal.valueOf(100)) == 0);
        model.addAttribute("totalWeightOver", totalWeight.compareTo(BigDecimal.valueOf(100)) > 0);

        return "admin/courses/score-components";
    }

    @GetMapping("/{courseId}/score-components/create")
    public String showCreateScoreComponentForm(
            @PathVariable Long courseId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Course course = courseService.findById(courseId).orElse(null);

        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khóa học");
            return "redirect:/admin/courses";
        }

        model.addAttribute("course", course);
        model.addAttribute("scoreComponentForm", new CourseScoreComponentForm());
        model.addAttribute("title", "Thêm thành phần điểm");
        model.addAttribute("actionUrl", "/admin/courses/" + courseId + "/score-components/create");

        return "admin/courses/score-component-form";
    }

    @PostMapping("/{courseId}/score-components/create")
    public String createScoreComponent(
            @PathVariable Long courseId,
            @Valid @ModelAttribute("scoreComponentForm") CourseScoreComponentForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Course course = courseService.findById(courseId).orElse(null);

        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khóa học");
            return "redirect:/admin/courses";
        }

        if (courseScoreComponentService.existsByCourseAndComponentCode(course, form.getComponentCode())) {
            bindingResult.rejectValue("componentCode", "duplicate", "Mã thành phần điểm đã tồn tại trong khóa học này");
        }

        if (isTotalWeightOver100(course, form.getWeightPercent(), form.getCalculated(), null)) {
            bindingResult.rejectValue("weightPercent", "invalid", "Tổng tỷ trọng điểm không được vượt quá 100%");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("course", course);
            model.addAttribute("title", "Thêm thành phần điểm");
            model.addAttribute("actionUrl", "/admin/courses/" + courseId + "/score-components/create");
            return "admin/courses/score-component-form";
        }

        CourseScoreComponent component = buildScoreComponentFromForm(form, course);
        courseScoreComponentService.save(component);

        redirectAttributes.addFlashAttribute("successMessage", "Thêm thành phần điểm thành công");
        return "redirect:/admin/courses/" + courseId + "/score-components";
    }

    @GetMapping("/{courseId}/score-components/{componentId}/edit")
    public String showEditScoreComponentForm(
            @PathVariable Long courseId,
            @PathVariable Long componentId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Course course = courseService.findById(courseId).orElse(null);
        CourseScoreComponent component = courseScoreComponentService.findById(componentId).orElse(null);

        if (course == null || component == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy dữ liệu cấu hình điểm");
            return "redirect:/admin/courses";
        }

        model.addAttribute("course", course);
        model.addAttribute("scoreComponentForm", buildFormFromScoreComponent(component));
        model.addAttribute("title", "Chỉnh sửa thành phần điểm");
        model.addAttribute("actionUrl", "/admin/courses/" + courseId + "/score-components/" + componentId + "/edit");

        return "admin/courses/score-component-form";
    }

    @PostMapping("/{courseId}/score-components/{componentId}/edit")
    public String updateScoreComponent(
            @PathVariable Long courseId,
            @PathVariable Long componentId,
            @Valid @ModelAttribute("scoreComponentForm") CourseScoreComponentForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Course course = courseService.findById(courseId).orElse(null);
        CourseScoreComponent existingComponent = courseScoreComponentService.findById(componentId).orElse(null);

        if (course == null || existingComponent == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy dữ liệu cấu hình điểm");
            return "redirect:/admin/courses";
        }

        if (!existingComponent.getComponentCode().equalsIgnoreCase(form.getComponentCode())
                && courseScoreComponentService.existsByCourseAndComponentCode(course, form.getComponentCode())) {
            bindingResult.rejectValue("componentCode", "duplicate", "Mã thành phần điểm đã tồn tại trong khóa học này");
        }

        if (isTotalWeightOver100(course, form.getWeightPercent(), form.getCalculated(), componentId)) {
            bindingResult.rejectValue("weightPercent", "invalid", "Tổng tỷ trọng điểm không được vượt quá 100%");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("course", course);
            model.addAttribute("title", "Chỉnh sửa thành phần điểm");
            model.addAttribute("actionUrl", "/admin/courses/" + courseId + "/score-components/" + componentId + "/edit");
            return "admin/courses/score-component-form";
        }

        CourseScoreComponent component = buildScoreComponentFromForm(form, course);
        courseScoreComponentService.update(componentId, component);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thành phần điểm thành công");
        return "redirect:/admin/courses/" + courseId + "/score-components";
    }

    @PostMapping("/{courseId}/score-components/{componentId}/delete")
    public String deleteScoreComponent(
            @PathVariable Long courseId,
            @PathVariable Long componentId,
            RedirectAttributes redirectAttributes
    ) {
        courseScoreComponentService.deleteById(componentId);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa thành phần điểm thành công");

        return "redirect:/admin/courses/" + courseId + "/score-components";
    }

    private Course buildCourseFromForm(CourseForm form) {
        CourseType courseType = courseTypeService.findById(form.getCourseTypeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại khóa học"));

        Course course = new Course();
        course.setCourseCode(form.getCourseCode().trim());
        course.setCourseName(form.getCourseName().trim());
        course.setCourseType(courseType);
        course.setDescription(form.getDescription());
        course.setActive(form.getActive() != null ? form.getActive() : true);

        return course;
    }

    private CourseForm buildFormFromCourse(Course course) {
        CourseForm form = new CourseForm();
        form.setId(course.getId());
        form.setCourseCode(course.getCourseCode());
        form.setCourseName(course.getCourseName());
        form.setCourseTypeId(course.getCourseType().getId());
        form.setDescription(course.getDescription());
        form.setActive(course.getActive());

        return form;
    }

    private CourseScoreComponent buildScoreComponentFromForm(
            CourseScoreComponentForm form,
            Course course
    ) {
        CourseScoreComponent component = new CourseScoreComponent();
        component.setCourse(course);
        component.setComponentCode(form.getComponentCode().trim());
        component.setComponentName(form.getComponentName().trim());
        component.setMaxScore(form.getMaxScore());
        component.setWeightPercent(form.getWeightPercent());
        component.setDisplayOrder(form.getDisplayOrder());
        component.setRequired(form.getRequired() != null ? form.getRequired() : false);
        component.setCalculated(form.getCalculated() != null ? form.getCalculated() : false);

        return component;
    }

    private CourseScoreComponentForm buildFormFromScoreComponent(CourseScoreComponent component) {
        CourseScoreComponentForm form = new CourseScoreComponentForm();
        form.setId(component.getId());
        form.setComponentCode(component.getComponentCode());
        form.setComponentName(component.getComponentName());
        form.setMaxScore(component.getMaxScore());
        form.setWeightPercent(component.getWeightPercent());
        form.setDisplayOrder(component.getDisplayOrder());
        form.setRequired(component.getRequired());
        form.setCalculated(component.getCalculated());

        return form;
    }

    private boolean isTotalWeightOver100(Course course,
                                         BigDecimal newWeight,
                                         Boolean calculated,
                                         Long ignoredComponentId) {
        if (newWeight == null || !Boolean.TRUE.equals(calculated)) {
            return false;
        }

        BigDecimal currentTotal = courseScoreComponentService.findByCourse(course).stream()
                .filter(component -> ignoredComponentId == null || !component.getId().equals(ignoredComponentId))
                .filter(component -> Boolean.TRUE.equals(component.getCalculated()))
                .map(CourseScoreComponent::getWeightPercent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return currentTotal.add(newWeight).compareTo(BigDecimal.valueOf(100)) > 0;
    }
}
