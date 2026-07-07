package com.englishcentermanager.controller;

import com.englishcentermanager.entity.Course;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.Role;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.repository.ClassStudentRepository;
import com.englishcentermanager.repository.ScoreEntryRepository;
import com.englishcentermanager.service.ClassScheduleService;
import com.englishcentermanager.service.CourseClassService;
import com.englishcentermanager.service.CourseService;
import com.englishcentermanager.service.RoleService;
import com.englishcentermanager.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/statistics")
public class AdminStatisticsController {
    private final UserService userService;
    private final RoleService roleService;
    private final CourseService courseService;
    private final CourseClassService courseClassService;
    private final ClassScheduleService classScheduleService;
    private final ClassStudentRepository classStudentRepository;
    private final ScoreEntryRepository scoreEntryRepository;

    public AdminStatisticsController(UserService userService,
                                     RoleService roleService,
                                     CourseService courseService,
                                     CourseClassService courseClassService,
                                     ClassScheduleService classScheduleService,
                                     ClassStudentRepository classStudentRepository,
                                     ScoreEntryRepository scoreEntryRepository) {
        this.userService = userService;
        this.roleService = roleService;
        this.courseService = courseService;
        this.courseClassService = courseClassService;
        this.classScheduleService = classScheduleService;
        this.classStudentRepository = classStudentRepository;
        this.scoreEntryRepository = scoreEntryRepository;
    }

    @GetMapping
    public String statistics(Model model) {
        List<CourseClass> classes = courseClassService.findAll();
        List<Course> courses = courseService.findAll();
        List<User> teachers = findUsersByRole("TEACHER");
        List<User> students = findUsersByRole("STUDENT");

        Map<Long, Long> studentCountsByClass = new HashMap<>();
        Map<Long, Double> averageScoresByClass = new HashMap<>();
        for (CourseClass courseClass : classes) {
            studentCountsByClass.put(courseClass.getId(), classStudentRepository.countByCourseClass(courseClass));
            averageScoresByClass.put(courseClass.getId(), scoreEntryRepository.averageScoreByClassId(courseClass.getId()));
        }

        Map<Long, Long> classCountsByTeacher = new HashMap<>();
        for (User teacher : teachers) {
            classCountsByTeacher.put(teacher.getId(), (long) courseClassService.findByTeacher(teacher).size());
        }

        Map<Long, Double> averageScoresByCourse = new HashMap<>();
        for (Course course : courses) {
            averageScoresByCourse.put(course.getId(), scoreEntryRepository.averageScoreByCourseId(course.getId()));
        }

        long activeClassCount = classes.stream()
                .filter(courseClass -> courseClass.getStatus() == enums.ClassStatus.OPEN
                        || courseClass.getStatus() == enums.ClassStatus.ONGOING
                        || courseClass.getStatus() == enums.ClassStatus.PLANNED)
                .count();

        model.addAttribute("totalTeachers", teachers.size());
        model.addAttribute("totalStudents", students.size());
        model.addAttribute("totalCourses", courses.size());
        model.addAttribute("activeClassCount", activeClassCount);
        model.addAttribute("totalSchedules", classScheduleService.findAll().size());
        model.addAttribute("classes", classes);
        model.addAttribute("courses", courses);
        model.addAttribute("teachers", teachers);
        model.addAttribute("studentCountsByClass", studentCountsByClass);
        model.addAttribute("classCountsByTeacher", classCountsByTeacher);
        model.addAttribute("averageScoresByClass", averageScoresByClass);
        model.addAttribute("averageScoresByCourse", averageScoresByCourse);

        return "admin/statistics/index";
    }

    private List<User> findUsersByRole(String roleName) {
        return roleService.findByName(roleName)
                .map(this::findUsersByRole)
                .orElse(List.of());
    }

    private List<User> findUsersByRole(Role role) {
        return userService.findByRole(role);
    }
}
