package com.englishcentermanager.controller;

import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.Role;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.repository.ClassStudentRepository;
import com.englishcentermanager.service.CourseClassService;
import com.englishcentermanager.service.CourseService;
import com.englishcentermanager.service.RoleService;
import com.englishcentermanager.service.RoomService;
import com.englishcentermanager.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AdminController {
    private final UserService userService;
    private final CourseService courseService;
    private final CourseClassService courseClassService;
    private final RoomService roomService;
    private final RoleService roleService;
    private final ClassStudentRepository classStudentRepository;

    public AdminController(UserService userService,
                           CourseService courseService,
                           CourseClassService courseClassService,
                           RoomService roomService,
                           RoleService roleService,
                           ClassStudentRepository classStudentRepository) {
        this.userService = userService;
        this.courseService = courseService;
        this.courseClassService = courseClassService;
        this.roomService = roomService;
        this.roleService = roleService;
        this.classStudentRepository = classStudentRepository;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        List<User> users = userService.findAll();
        List<CourseClass> classes = courseClassService.findAll();

        long openClassCount = classes.stream()
                .filter(courseClass -> courseClass.getStatus() == enums.ClassStatus.OPEN
                        || courseClass.getStatus() == enums.ClassStatus.ONGOING
                        || courseClass.getStatus() == enums.ClassStatus.PLANNED)
                .count();

        Map<enums.ClassStatus, Long> classStatusCounts = classes.stream()
                .collect(Collectors.groupingBy(CourseClass::getStatus, Collectors.counting()));

        List<CourseClass> recentClasses = classes.stream()
                .sorted((first, second) -> second.getId().compareTo(first.getId()))
                .limit(6)
                .toList();

        model.addAttribute("totalAccounts", users.size());
        model.addAttribute("activeCourses", courseService.findAllActive().size());
        model.addAttribute("openClasses", openClassCount);
        model.addAttribute("activeRooms", roomService.findAllActive().size());
        model.addAttribute("teacherCount", countUsersByRole("TEACHER"));
        model.addAttribute("studentCount", countUsersByRole("STUDENT"));
        model.addAttribute("plannedClassCount", classStatusCounts.getOrDefault(enums.ClassStatus.PLANNED, 0L));
        model.addAttribute("openClassCount", classStatusCounts.getOrDefault(enums.ClassStatus.OPEN, 0L));
        model.addAttribute("ongoingClassCount", classStatusCounts.getOrDefault(enums.ClassStatus.ONGOING, 0L));
        model.addAttribute("completedClassCount", classStatusCounts.getOrDefault(enums.ClassStatus.COMPLETED, 0L));
        model.addAttribute("recentClasses", recentClasses);
        model.addAttribute("classStudentRepository", classStudentRepository);

        return "admin/dashboard-v2";
    }

    private long countUsersByRole(String roleName) {
        return roleService.findByName(roleName)
                .map(this::countUsersByRole)
                .orElse(0L);
    }

    private long countUsersByRole(Role role) {
        return userService.findByRole(role).size();
    }
}
