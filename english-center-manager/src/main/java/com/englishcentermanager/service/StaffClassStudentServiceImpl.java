package com.englishcentermanager.service;

import com.englishcentermanager.entity.ClassStudent;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.StudentStatusHistory;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.repository.ClassStudentRepository;
import com.englishcentermanager.repository.CourseClassRepository;
import com.englishcentermanager.repository.StudentStatusHistoryRepository;
import com.englishcentermanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StaffClassStudentServiceImpl implements StaffClassStudentService {
    private final ClassStudentRepository classStudentRepository;
    private final CourseClassRepository courseClassRepository;
    private final UserRepository userRepository;
    private final StudentStatusHistoryRepository studentStatusHistoryRepository;

    public StaffClassStudentServiceImpl(ClassStudentRepository classStudentRepository,
                                        CourseClassRepository courseClassRepository,
                                        UserRepository userRepository,
                                        StudentStatusHistoryRepository studentStatusHistoryRepository) {
        this.classStudentRepository = classStudentRepository;
        this.courseClassRepository = courseClassRepository;
        this.userRepository = userRepository;
        this.studentStatusHistoryRepository = studentStatusHistoryRepository;
    }

    @Override
    public List<ClassStudent> searchInClass(Long classId, String keyword, enums.StudentClassStatus status) {
        String normalizedKeyword = keyword == null || keyword.trim().isEmpty() ? null : keyword.trim();
        return classStudentRepository.searchInClass(classId, normalizedKeyword, status);
    }

    @Override
    @Transactional
    public ClassStudent addStudentToClass(Long classId, Long studentId, LocalDate joinedAt, User changedBy) {
        CourseClass courseClass = findClass(classId);
        User student = findUser(studentId);
        validateStudentRole(student);

        if (classStudentRepository.existsByCourseClassAndStudent(courseClass, student)) {
            throw new IllegalArgumentException("Hoc vien da co trong lop nay.");
        }

        ClassStudent classStudent = new ClassStudent();
        classStudent.setCourseClass(courseClass);
        classStudent.setStudent(student);
        classStudent.setJoinedAt(joinedAt == null ? LocalDate.now() : joinedAt);
        classStudent.setStatus(enums.StudentClassStatus.STUDYING);

        ClassStudent savedClassStudent = classStudentRepository.save(classStudent);
        recordStatusHistory(savedClassStudent, null, savedClassStudent.getStatus(), changedBy, "Them hoc vien vao lop");

        return savedClassStudent;
    }

    @Override
    @Transactional
    public void updateStatus(Long classStudentId,
                             enums.StudentClassStatus status,
                             User changedBy,
                             String note) {
        ClassStudent classStudent = findClassStudent(classStudentId);
        enums.StudentClassStatus oldStatus = classStudent.getStatus();

        classStudent.setStatus(status);
        if (status == enums.StudentClassStatus.DROPPED || status == enums.StudentClassStatus.COMPLETED) {
            classStudent.setLeftAt(LocalDate.now());
        } else if (status == enums.StudentClassStatus.STUDYING) {
            classStudent.setLeftAt(null);
        }

        classStudentRepository.save(classStudent);
        recordStatusHistory(classStudent, oldStatus, status, changedBy, note);
    }

    @Override
    @Transactional
    public ClassStudent transferStudent(Long classStudentId, Long targetClassId, User changedBy, String note) {
        ClassStudent sourceClassStudent = findClassStudent(classStudentId);
        CourseClass targetClass = findClass(targetClassId);

        if (sourceClassStudent.getCourseClass().getId().equals(targetClassId)) {
            throw new IllegalArgumentException("Lop chuyen den phai khac lop hien tai.");
        }

        if (classStudentRepository.existsByCourseClassAndStudent(targetClass, sourceClassStudent.getStudent())) {
            throw new IllegalArgumentException("Hoc vien da co trong lop chuyen den.");
        }

        enums.StudentClassStatus oldStatus = sourceClassStudent.getStatus();
        sourceClassStudent.setStatus(enums.StudentClassStatus.WAITING_TRANSFER);
        sourceClassStudent.setLeftAt(LocalDate.now());
        classStudentRepository.save(sourceClassStudent);
        recordStatusHistory(sourceClassStudent, oldStatus, enums.StudentClassStatus.WAITING_TRANSFER, changedBy, note);

        ClassStudent targetClassStudent = new ClassStudent();
        targetClassStudent.setCourseClass(targetClass);
        targetClassStudent.setStudent(sourceClassStudent.getStudent());
        targetClassStudent.setJoinedAt(LocalDate.now());
        targetClassStudent.setStatus(enums.StudentClassStatus.STUDYING);

        ClassStudent savedTarget = classStudentRepository.save(targetClassStudent);
        recordStatusHistory(savedTarget, null, enums.StudentClassStatus.STUDYING, changedBy,
                "Nhan chuyen lop tu " + sourceClassStudent.getCourseClass().getClassCode());

        return savedTarget;
    }

    private CourseClass findClass(Long classId) {
        return courseClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hoc"));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay hoc vien"));
    }

    private ClassStudent findClassStudent(Long classStudentId) {
        return classStudentRepository.findById(classStudentId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay hoc vien trong lop"));
    }

    private void validateStudentRole(User user) {
        if (user.getRole() == null || !"STUDENT".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalArgumentException("Tai khoan duoc chon khong phai hoc vien.");
        }
    }

    private void recordStatusHistory(ClassStudent classStudent,
                                     enums.StudentClassStatus oldStatus,
                                     enums.StudentClassStatus newStatus,
                                     User changedBy,
                                     String note) {
        StudentStatusHistory history = new StudentStatusHistory();
        history.setClassStudent(classStudent);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedByUser(changedBy);
        history.setChangedAt(LocalDateTime.now());
        history.setNote(note);
        studentStatusHistoryRepository.save(history);
    }
}
