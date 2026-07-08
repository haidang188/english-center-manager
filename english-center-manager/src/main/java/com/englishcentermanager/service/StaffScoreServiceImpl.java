package com.englishcentermanager.service;

import com.englishcentermanager.dto.ExamSessionForm;
import com.englishcentermanager.dto.StaffScoreBoard;
import com.englishcentermanager.entity.ClassStudent;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.CourseScoreComponent;
import com.englishcentermanager.entity.ExamSession;
import com.englishcentermanager.entity.ScoreEntry;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.repository.ClassStudentRepository;
import com.englishcentermanager.repository.CourseClassRepository;
import com.englishcentermanager.repository.CourseScoreComponentRepository;
import com.englishcentermanager.repository.ExamSessionRepository;
import com.englishcentermanager.repository.ScoreEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StaffScoreServiceImpl implements StaffScoreService {
    private final CourseClassRepository courseClassRepository;
    private final ClassStudentRepository classStudentRepository;
    private final CourseScoreComponentRepository courseScoreComponentRepository;
    private final ExamSessionRepository examSessionRepository;
    private final ScoreEntryRepository scoreEntryRepository;

    public StaffScoreServiceImpl(CourseClassRepository courseClassRepository,
                                 ClassStudentRepository classStudentRepository,
                                 CourseScoreComponentRepository courseScoreComponentRepository,
                                 ExamSessionRepository examSessionRepository,
                                 ScoreEntryRepository scoreEntryRepository) {
        this.courseClassRepository = courseClassRepository;
        this.classStudentRepository = classStudentRepository;
        this.courseScoreComponentRepository = courseScoreComponentRepository;
        this.examSessionRepository = examSessionRepository;
        this.scoreEntryRepository = scoreEntryRepository;
    }

    @Override
    public List<ExamSession> findSessions(CourseClass courseClass) {
        return examSessionRepository.findByCourseClassOrderByExamDateDescIdDesc(courseClass);
    }

    @Override
    public Optional<ExamSession> findLatestSession(CourseClass courseClass) {
        return examSessionRepository.findFirstByCourseClassOrderByExamDateDescIdDesc(courseClass);
    }

    @Override
    @Transactional
    public ExamSession createSession(Long classId, ExamSessionForm form, User createdBy) {
        CourseClass courseClass = findClass(classId);

        ExamSession examSession = new ExamSession();
        examSession.setCourseClass(courseClass);
        examSession.setExamName(form.getExamName().trim());
        examSession.setExamDate(form.getExamDate());
        examSession.setNote(form.getNote());
        examSession.setCreatedByUser(createdBy);
        examSession.setCreatedAt(LocalDateTime.now());

        return examSessionRepository.save(examSession);
    }

    @Override
    public StaffScoreBoard buildScoreBoard(Long classId, Long examSessionId) {
        CourseClass courseClass = findClass(classId);
        ExamSession examSession = findSessionInClass(examSessionId, courseClass);
        List<ClassStudent> classStudents = classStudentRepository.findByCourseClassOrderByStudentFullNameAsc(courseClass);
        List<CourseScoreComponent> components = loadComponents(courseClass);
        List<CourseScoreComponent> inputComponents = components.stream()
                .filter(component -> !Boolean.TRUE.equals(component.getCalculated()))
                .toList();
        List<CourseScoreComponent> calculatedComponents = components.stream()
                .filter(component -> Boolean.TRUE.equals(component.getCalculated()))
                .toList();

        StaffScoreBoard scoreBoard = new StaffScoreBoard();
        scoreBoard.setCourseClass(courseClass);
        scoreBoard.setExamSession(examSession);
        scoreBoard.setClassStudents(classStudents);
        scoreBoard.setInputComponents(inputComponents);
        scoreBoard.setCalculatedComponents(calculatedComponents);

        if (classStudents.isEmpty()) {
            return scoreBoard;
        }

        List<ScoreEntry> entries = scoreEntryRepository.findByExamSessionAndClassStudentIn(examSession, classStudents);
        for (ScoreEntry entry : entries) {
            scoreBoard.getScoreValues().put(scoreKey(entry.getClassStudent().getId(), entry.getScoreComponent().getId()),
                    entry.getScoreValue());
        }

        for (ClassStudent classStudent : classStudents) {
            calculatedComponents.stream()
                    .map(component -> scoreBoard.getScoreValues().get(scoreKey(classStudent.getId(), component.getId())))
                    .filter(value -> value != null)
                    .findFirst()
                    .or(() -> calculateAverage(classStudent, inputComponents, scoreBoard.getScoreValues()))
                    .ifPresent(value -> scoreBoard.getAverageScores().put(classStudent.getId(), value));
        }

        return scoreBoard;
    }

    @Override
    @Transactional
    public void saveScores(Long classId, Long examSessionId, Map<String, String> params, User currentUser) {
        CourseClass courseClass = findClass(classId);
        ExamSession examSession = findSessionInClass(examSessionId, courseClass);
        List<ClassStudent> classStudents = classStudentRepository.findByCourseClassOrderByStudentFullNameAsc(courseClass);
        Map<Long, ClassStudent> classStudentById = new HashMap<>();
        for (ClassStudent classStudent : classStudents) {
            classStudentById.put(classStudent.getId(), classStudent);
        }

        List<CourseScoreComponent> components = loadComponents(courseClass);
        Map<Long, CourseScoreComponent> componentById = new HashMap<>();
        for (CourseScoreComponent component : components) {
            componentById.put(component.getId(), component);
        }

        for (Map.Entry<String, String> param : params.entrySet()) {
            if (!param.getKey().startsWith("score_") || !hasText(param.getValue())) {
                continue;
            }

            String[] keyParts = param.getKey().split("_");
            if (keyParts.length != 3) {
                continue;
            }

            Long classStudentId = Long.valueOf(keyParts[1]);
            Long componentId = Long.valueOf(keyParts[2]);
            ClassStudent classStudent = classStudentById.get(classStudentId);
            CourseScoreComponent component = componentById.get(componentId);

            if (classStudent == null || component == null || Boolean.TRUE.equals(component.getCalculated())) {
                continue;
            }

            BigDecimal scoreValue = parseScore(param.getValue(), component);
            upsertScore(examSession, classStudent, component, scoreValue, currentUser);
        }

        saveCalculatedScores(examSession, classStudents, components, currentUser);
    }

    private void saveCalculatedScores(ExamSession examSession,
                                      List<ClassStudent> classStudents,
                                      List<CourseScoreComponent> components,
                                      User currentUser) {
        List<CourseScoreComponent> inputComponents = components.stream()
                .filter(component -> !Boolean.TRUE.equals(component.getCalculated()))
                .toList();
        List<CourseScoreComponent> calculatedComponents = components.stream()
                .filter(component -> Boolean.TRUE.equals(component.getCalculated()))
                .toList();

        if (calculatedComponents.isEmpty()) {
            return;
        }

        for (ClassStudent classStudent : classStudents) {
            Map<String, BigDecimal> scoreValues = new HashMap<>();
            List<ScoreEntry> entries = scoreEntryRepository.findByExamSessionAndClassStudent(examSession, classStudent);
            for (ScoreEntry entry : entries) {
                scoreValues.put(scoreKey(classStudent.getId(), entry.getScoreComponent().getId()), entry.getScoreValue());
            }

            Optional<BigDecimal> average = calculateAverage(classStudent, inputComponents, scoreValues);
            if (average.isEmpty()) {
                continue;
            }

            for (CourseScoreComponent calculatedComponent : calculatedComponents) {
                upsertScore(examSession, classStudent, calculatedComponent, average.get(), currentUser);
            }
        }
    }

    private Optional<BigDecimal> calculateAverage(ClassStudent classStudent,
                                                  List<CourseScoreComponent> inputComponents,
                                                  Map<String, BigDecimal> scoreValues) {
        if (inputComponents.isEmpty()) {
            return Optional.empty();
        }

        BigDecimal weightedTotal = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal simpleTotal = BigDecimal.ZERO;
        int scoredCount = 0;

        for (CourseScoreComponent component : inputComponents) {
            BigDecimal scoreValue = scoreValues.get(scoreKey(classStudent.getId(), component.getId()));

            if (scoreValue == null && Boolean.TRUE.equals(component.getRequired())) {
                return Optional.empty();
            }

            if (scoreValue == null) {
                continue;
            }

            BigDecimal weight = component.getWeightPercent();
            if (weight != null && weight.compareTo(BigDecimal.ZERO) > 0) {
                weightedTotal = weightedTotal.add(scoreValue.multiply(weight));
                totalWeight = totalWeight.add(weight);
            } else {
                simpleTotal = simpleTotal.add(scoreValue);
                scoredCount++;
            }
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            return Optional.of(weightedTotal.divide(totalWeight, 2, RoundingMode.HALF_UP));
        }

        if (scoredCount == 0) {
            return Optional.empty();
        }

        return Optional.of(simpleTotal.divide(BigDecimal.valueOf(scoredCount), 2, RoundingMode.HALF_UP));
    }

    private void upsertScore(ExamSession examSession,
                             ClassStudent classStudent,
                             CourseScoreComponent component,
                             BigDecimal scoreValue,
                             User currentUser) {
        ScoreEntry scoreEntry = scoreEntryRepository
                .findByExamSessionAndClassStudentAndScoreComponent(examSession, classStudent, component)
                .orElseGet(() -> {
                    ScoreEntry newEntry = new ScoreEntry();
                    newEntry.setExamSession(examSession);
                    newEntry.setClassStudent(classStudent);
                    newEntry.setScoreComponent(component);
                    newEntry.setCreatedByTeacher(currentUser);
                    newEntry.setCreatedAt(LocalDateTime.now());
                    return newEntry;
                });

        if (scoreEntry.getId() != null) {
            scoreEntry.setUpdatedByTeacher(currentUser);
            scoreEntry.setUpdatedAt(LocalDateTime.now());
        }

        scoreEntry.setScoreValue(scoreValue);
        scoreEntryRepository.save(scoreEntry);
    }

    private BigDecimal parseScore(String rawScore, CourseScoreComponent component) {
        try {
            BigDecimal scoreValue = new BigDecimal(rawScore.trim().replace(',', '.'));
            if (scoreValue.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Diem khong duoc am.");
            }
            if (component.getMaxScore() != null && scoreValue.compareTo(component.getMaxScore()) > 0) {
                throw new IllegalArgumentException("Diem cua " + component.getComponentName()
                        + " khong duoc vuot qua " + component.getMaxScore() + ".");
            }
            return scoreValue.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Diem nhap vao khong hop le.");
        }
    }

    private List<CourseScoreComponent> loadComponents(CourseClass courseClass) {
        return courseScoreComponentRepository.findByCourse(courseClass.getCourse()).stream()
                .sorted(Comparator.comparing(
                        component -> component.getDisplayOrder() == null ? Integer.MAX_VALUE : component.getDisplayOrder()))
                .toList();
    }

    private CourseClass findClass(Long classId) {
        return courseClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hoc"));
    }

    private ExamSession findSessionInClass(Long examSessionId, CourseClass courseClass) {
        ExamSession examSession = examSessionRepository.findById(examSessionId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay dot diem"));

        if (!examSession.getCourseClass().getId().equals(courseClass.getId())) {
            throw new IllegalArgumentException("Dot diem khong thuoc lop hoc nay.");
        }

        return examSession;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String scoreKey(Long classStudentId, Long componentId) {
        return classStudentId + "_" + componentId;
    }
}
