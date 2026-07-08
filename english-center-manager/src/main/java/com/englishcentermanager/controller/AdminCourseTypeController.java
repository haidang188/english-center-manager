package com.englishcentermanager.controller;

import com.englishcentermanager.dto.CourseTypeForm;
import com.englishcentermanager.entity.CourseType;
import com.englishcentermanager.service.CourseTypeService;
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
@RequestMapping("/admin/course-types")
public class AdminCourseTypeController {
    private static final int PAGE_SIZE = 8;

    private final CourseTypeService courseTypeService;

    public AdminCourseTypeController(CourseTypeService courseTypeService) {
        this.courseTypeService = courseTypeService;
    }

    @GetMapping
    public String listCourseTypes(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Boolean active,
                                  @RequestParam(defaultValue = "0") int page,
                                  Model model) {
        List<CourseType> courseTypes = courseTypeService.findAll();

        if (hasText(keyword)) {
            String searchValue = keyword.trim().toLowerCase();
            courseTypes = courseTypes.stream()
                    .filter(type -> type.getTypeCode().toLowerCase().contains(searchValue)
                            || type.getTypeName().toLowerCase().contains(searchValue))
                    .toList();
        }

        if (active != null) {
            courseTypes = courseTypes.stream()
                    .filter(type -> Boolean.TRUE.equals(type.getActive()) == active)
                    .toList();
        }

        courseTypes = courseTypes.stream()
                .sorted(Comparator.comparing(CourseType::getId).reversed())
                .toList();

        Page<CourseType> courseTypePage = toPage(courseTypes, page);

        model.addAttribute("courseTypePage", courseTypePage);
        model.addAttribute("courseTypes", courseTypePage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("active", active);

        return "admin/course-types/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        CourseTypeForm courseTypeForm = new CourseTypeForm();
        courseTypeForm.setActive(true);

        model.addAttribute("courseTypeForm", courseTypeForm);
        model.addAttribute("isEdit", false);

        return "admin/course-types/form";
    }

    @PostMapping("/create")
    public String createCourseType(@Valid @ModelAttribute("courseTypeForm") CourseTypeForm courseTypeForm,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (courseTypeService.existsByTypeCode(courseTypeForm.getTypeCode())) {
            bindingResult.rejectValue("typeCode", "duplicate", "Mã loại khóa học đã tồn tại");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "admin/course-types/form";
        }

        courseTypeService.save(toEntity(courseTypeForm));
        redirectAttributes.addFlashAttribute("successMessage", "Thêm loại khóa học thành công.");

        return "redirect:/admin/course-types";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        CourseType courseType = courseTypeService.findById(id).orElse(null);

        if (courseType == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy loại khóa học.");
            return "redirect:/admin/course-types";
        }

        model.addAttribute("courseTypeForm", toForm(courseType));
        model.addAttribute("isEdit", true);

        return "admin/course-types/form";
    }

    @PostMapping("/{id}/edit")
    public String updateCourseType(@PathVariable Long id,
                                   @Valid @ModelAttribute("courseTypeForm") CourseTypeForm courseTypeForm,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        CourseType existingCourseType = courseTypeService.findById(id).orElse(null);

        if (existingCourseType == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy loại khóa học.");
            return "redirect:/admin/course-types";
        }

        courseTypeService.findByTypeCode(courseTypeForm.getTypeCode())
                .filter(type -> !type.getId().equals(id))
                .ifPresent(type -> bindingResult.rejectValue("typeCode", "duplicate", "Mã loại khóa học đã tồn tại"));

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "admin/course-types/form";
        }

        courseTypeService.update(id, toEntity(courseTypeForm));
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật loại khóa học thành công.");

        return "redirect:/admin/course-types";
    }

    @PostMapping("/{id}/toggle")
    public String toggleCourseTypeStatus(@PathVariable Long id,
                                         RedirectAttributes redirectAttributes) {
        CourseType courseType = courseTypeService.findById(id).orElse(null);

        if (courseType == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy loại khóa học.");
            return "redirect:/admin/course-types";
        }

        if (Boolean.TRUE.equals(courseType.getActive())) {
            courseTypeService.deactivate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã ngưng sử dụng loại khóa học.");
        } else {
            courseTypeService.activate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã kích hoạt loại khóa học.");
        }

        return "redirect:/admin/course-types";
    }

    private Page<CourseType> toPage(List<CourseType> courseTypes, int page) {
        int safePage = Math.max(page, 0);
        int start = Math.min(safePage * PAGE_SIZE, courseTypes.size());
        int end = Math.min(start + PAGE_SIZE, courseTypes.size());

        return new PageImpl<>(
                courseTypes.subList(start, end),
                PageRequest.of(safePage, PAGE_SIZE),
                courseTypes.size()
        );
    }

    private CourseType toEntity(CourseTypeForm form) {
        CourseType courseType = new CourseType();
        courseType.setTypeCode(form.getTypeCode().trim());
        courseType.setTypeName(form.getTypeName().trim());
        courseType.setDescription(form.getDescription());
        courseType.setActive(form.getActive() != null ? form.getActive() : true);

        return courseType;
    }

    private CourseTypeForm toForm(CourseType courseType) {
        CourseTypeForm form = new CourseTypeForm();
        form.setId(courseType.getId());
        form.setTypeCode(courseType.getTypeCode());
        form.setTypeName(courseType.getTypeName());
        form.setDescription(courseType.getDescription());
        form.setActive(courseType.getActive());

        return form;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
