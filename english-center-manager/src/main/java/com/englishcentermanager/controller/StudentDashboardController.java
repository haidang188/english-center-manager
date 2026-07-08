package com.englishcentermanager.controller;

import com.englishcentermanager.entity.ClassStudent;
import com.englishcentermanager.entity.ExamSession;
import com.englishcentermanager.entity.ScoreEntry;
import com.englishcentermanager.entity.StudentTuition;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.repository.ClassStudentRepository;
import com.englishcentermanager.repository.PaymentHistoryRepository;
import com.englishcentermanager.repository.ScoreEntryRepository;
import com.englishcentermanager.repository.StudentTuitionRepository;
import com.englishcentermanager.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class StudentDashboardController {
    private final UserService userService;
    private final ClassStudentRepository classStudentRepository;
    private final ScoreEntryRepository scoreEntryRepository;
    private final StudentTuitionRepository studentTuitionRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    public StudentDashboardController(UserService userService,
                                      ClassStudentRepository classStudentRepository,
                                      ScoreEntryRepository scoreEntryRepository,
                                      StudentTuitionRepository studentTuitionRepository,
                                      PaymentHistoryRepository paymentHistoryRepository) {
        this.userService = userService;
        this.classStudentRepository = classStudentRepository;
        this.scoreEntryRepository = scoreEntryRepository;
        this.studentTuitionRepository = studentTuitionRepository;
        this.paymentHistoryRepository = paymentHistoryRepository;
    }

    @GetMapping({"/student", "/student/dashboard"})
    public String dashboard(Authentication authentication, Model model) {
        User student = currentUser(authentication);
        List<ClassStudent> enrollments = enrollments(student);
        List<StudentTuition> tuitions = studentTuitionRepository.findByStudentOrderByCreatedAtDesc(student);

        model.addAttribute("student", student);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("activeClassCount", enrollments.stream()
                .filter(item -> item.getStatus() == enums.StudentClassStatus.STUDYING)
                .count());
        model.addAttribute("unpaidTuitionCount", tuitions.stream()
                .filter(item -> item.getStatus() == enums.TuitionStatus.UNPAID
                        || item.getStatus() == enums.TuitionStatus.OVERDUE)
                .count());
        return "student/dashboard";
    }

    @GetMapping("/student/classes")
    public String classes(Authentication authentication, Model model) {
        model.addAttribute("enrollments", enrollments(currentUser(authentication)));
        return "student/classes";
    }

    @GetMapping("/student/classes/{classStudentId}")
    public String classDetail(@PathVariable Long classStudentId,
                              Authentication authentication,
                              Model model) {
        ClassStudent classStudent = ownEnrollment(classStudentId, authentication);
        model.addAttribute("classStudent", classStudent);
        model.addAttribute("scoreGroups", buildScoreGroups(classStudent));
        return "student/class-detail";
    }

    @GetMapping("/student/scores")
    public String scores(Authentication authentication, Model model) {
        List<ClassStudent> enrollments = enrollments(currentUser(authentication));
        List<StudentScoreView> scoreViews = enrollments.stream()
                .map(enrollment -> new StudentScoreView(enrollment, buildScoreGroups(enrollment)))
                .toList();

        model.addAttribute("enrollments", enrollments);
        model.addAttribute("scoreViews", scoreViews);
        return "student/scores";
    }

    @GetMapping("/student/tuition")
    public String tuition(Authentication authentication, Model model) {
        User student = currentUser(authentication);
        model.addAttribute("tuitions", studentTuitionRepository.findByStudentOrderByCreatedAtDesc(student));
        model.addAttribute("paymentHistories", paymentHistoryRepository.findByStudentOrderByPaidAtDesc(student));
        return "student/tuition";
    }

    @GetMapping("/student/tuition/{tuitionId}/history")
    public String tuitionHistory(@PathVariable Long tuitionId,
                                 Authentication authentication,
                                 Model model) {
        User student = currentUser(authentication);
        StudentTuition tuition = studentTuitionRepository.findById(tuitionId)
                .filter(item -> item.getStudent().getId().equals(student.getId()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoản học phí."));

        model.addAttribute("tuition", tuition);
        model.addAttribute("histories", paymentHistoryRepository.findByStudentTuitionOrderByPaidAtDesc(tuition));
        return "student/tuition-history";
    }

    private List<ClassStudent> enrollments(User student) {
        return classStudentRepository.findByStudent(student).stream()
                .sorted(Comparator.comparing(item -> item.getCourseClass().getClassName()))
                .toList();
    }

    private ClassStudent ownEnrollment(Long classStudentId, Authentication authentication) {
        User student = currentUser(authentication);
        return classStudentRepository.findById(classStudentId)
                .filter(item -> item.getStudent().getId().equals(student.getId()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học của học viên."));
    }

    private List<ScoreGroup> buildScoreGroups(ClassStudent classStudent) {
        Map<ExamSession, List<ScoreEntry>> grouped = new LinkedHashMap<>();
        for (ScoreEntry entry : scoreEntryRepository.findByClassStudentForDisplay(classStudent)) {
            grouped.computeIfAbsent(entry.getExamSession(), key -> new java.util.ArrayList<>()).add(entry);
        }

        return grouped.entrySet().stream()
                .map(entry -> new ScoreGroup(entry.getKey(), entry.getValue(), average(entry.getValue())))
                .toList();
    }

    private BigDecimal average(List<ScoreEntry> entries) {
        List<ScoreEntry> resultEntries = entries.stream()
                .filter(entry -> !Boolean.TRUE.equals(entry.getScoreComponent().getCalculated()))
                .toList();
        List<ScoreEntry> source = resultEntries.isEmpty() ? entries : resultEntries;

        if (source.isEmpty()) {
            return null;
        }

        BigDecimal total = source.stream()
                .map(ScoreEntry::getScoreValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(source.size()), 2, RoundingMode.HALF_UP);
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Không xác định được tài khoản đăng nhập.");
        }

        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản đăng nhập."));
    }

    public static class ScoreGroup {
        private final ExamSession examSession;
        private final List<ScoreEntry> entries;
        private final BigDecimal averageScore;

        public ScoreGroup(ExamSession examSession, List<ScoreEntry> entries, BigDecimal averageScore) {
            this.examSession = examSession;
            this.entries = entries;
            this.averageScore = averageScore;
        }

        public ExamSession getExamSession() {
            return examSession;
        }

        public List<ScoreEntry> getEntries() {
            return entries;
        }

        public BigDecimal getAverageScore() {
            return averageScore;
        }
    }

    public static class StudentScoreView {
        private final ClassStudent enrollment;
        private final List<ScoreGroup> scoreGroups;

        public StudentScoreView(ClassStudent enrollment, List<ScoreGroup> scoreGroups) {
            this.enrollment = enrollment;
            this.scoreGroups = scoreGroups;
        }

        public ClassStudent getEnrollment() {
            return enrollment;
        }

        public List<ScoreGroup> getScoreGroups() {
            return scoreGroups;
        }
    }
}
