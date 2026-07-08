package com.englishcentermanager.controller;

import com.englishcentermanager.entity.Course;
import com.englishcentermanager.entity.CourseScoreComponent;
import com.englishcentermanager.service.CourseScoreComponentService;
import com.englishcentermanager.service.CourseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/score-components")
public class AdminScoreComponentController {
    private final CourseService courseService;
    private final CourseScoreComponentService courseScoreComponentService;

    public AdminScoreComponentController(CourseService courseService,
                                         CourseScoreComponentService courseScoreComponentService) {
        this.courseService = courseService;
        this.courseScoreComponentService = courseScoreComponentService;
    }

    @GetMapping
    public String listScoreComponentOverview(Model model) {
        List<Course> courses = courseService.findAll().stream()
                .sorted(Comparator.comparing(Course::getCourseCode))
                .toList();

        Map<Long, String> formulasByCourse = new LinkedHashMap<>();
        for (Course course : courses) {
            formulasByCourse.put(course.getId(), buildScoreFormula(course));
        }

        model.addAttribute("courses", courses);
        model.addAttribute("formulasByCourse", formulasByCourse);

        return "admin/score-components/list";
    }

    private String buildScoreFormula(Course course) {
        List<CourseScoreComponent> calculatedComponents =
                courseScoreComponentService.findByCourseOrderByDisplayOrder(course).stream()
                        .filter(component -> Boolean.TRUE.equals(component.getCalculated()))
                        .toList();

        if (calculatedComponents.isEmpty()) {
            return "Chưa cấu hình công thức điểm.";
        }

        String expression = calculatedComponents.stream()
                .map(component -> component.getComponentName()
                        + " x "
                        + formatPercent(component.getWeightPercent())
                        + "%")
                .reduce((left, right) -> left + " + " + right)
                .orElse("");

        return "Điểm trung bình = " + expression;
    }

    private String formatPercent(BigDecimal value) {
        if (value == null) {
            return "0";
        }

        return value.stripTrailingZeros().toPlainString();
    }
}
