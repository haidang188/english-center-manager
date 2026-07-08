package com.englishcentermanager.controller;

import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.repository.ClassStudentRepository;
import com.englishcentermanager.repository.StudentTuitionRepository;
import com.englishcentermanager.service.CourseClassService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class StaffDashboardController {
    private final CourseClassService courseClassService;
    private final ClassStudentRepository classStudentRepository;
    private final StudentTuitionRepository studentTuitionRepository;

    public StaffDashboardController(CourseClassService courseClassService,
                                    ClassStudentRepository classStudentRepository,
                                    StudentTuitionRepository studentTuitionRepository) {
        this.courseClassService = courseClassService;
        this.classStudentRepository = classStudentRepository;
        this.studentTuitionRepository = studentTuitionRepository;
    }

    @GetMapping({"/staff", "/staff/dashboard"})
    public String dashboard(Model model) {
        List<CourseClass> classes = courseClassService.findAll();
        List<CourseClass> recentClasses = classes.stream()
                .sorted((first, second) -> second.getId().compareTo(first.getId()))
                .limit(6)
                .toList();

        long activeClasses = classes.stream()
                .filter(courseClass -> courseClass.getStatus() == enums.ClassStatus.OPEN
                        || courseClass.getStatus() == enums.ClassStatus.ONGOING
                        || courseClass.getStatus() == enums.ClassStatus.PLANNED)
                .count();

        model.addAttribute("totalClasses", classes.size());
        model.addAttribute("activeClasses", activeClasses);
        model.addAttribute("totalClassStudents", classStudentRepository.count());
        model.addAttribute("unpaidTuitions", studentTuitionRepository.countByStatus(enums.TuitionStatus.UNPAID));
        model.addAttribute("recentClasses", recentClasses);
        model.addAttribute("classStudentRepository", classStudentRepository);

        return "staff/dashboard";
    }
}
