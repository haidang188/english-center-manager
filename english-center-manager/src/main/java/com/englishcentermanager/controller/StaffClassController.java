package com.englishcentermanager.controller;

import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.repository.ClassStudentRepository;
import com.englishcentermanager.service.CourseService;
import com.englishcentermanager.service.CourseClassService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/staff/classes")
public class StaffClassController {
    private static final int PAGE_SIZE = 8;

    private final CourseClassService courseClassService;
    private final CourseService courseService;
    private final ClassStudentRepository classStudentRepository;

    public StaffClassController(CourseClassService courseClassService,
                                CourseService courseService,
                                ClassStudentRepository classStudentRepository) {
        this.courseClassService = courseClassService;
        this.courseService = courseService;
        this.classStudentRepository = classStudentRepository;
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
                    .filter(courseClass -> contains(courseClass.getClassCode(), searchValue)
                            || contains(courseClass.getClassName(), searchValue)
                            || contains(courseClass.getTeacher().getFullName(), searchValue))
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
        model.addAttribute("classStudentRepository", classStudentRepository);

        return "staff/classes";
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

    private boolean contains(String source, String keyword) {
        return source != null && source.toLowerCase().contains(keyword);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
