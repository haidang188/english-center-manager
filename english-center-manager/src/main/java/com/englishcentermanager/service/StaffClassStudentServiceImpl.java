package com.englishcentermanager.service;

import com.englishcentermanager.entity.*;
import com.englishcentermanager.exception.BusinessException;
import com.englishcentermanager.exception.ResourceNotFoundException;
import com.englishcentermanager.repository.*;

import com.englishcentermanager.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffClassStudentServiceImpl implements StaffClassStudentService {

    private final ClassStudentRepository classStudentRepository;
    private final CourseClassRepository courseClassRepository;
    private final UserRepository userRepository;
    private final StudentStatusHistoryRepository studentStatusHistoryRepository;
    private final SecurityService securityService;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ClassStudent> getStudentsByClass(Long classId, Pageable pageable) {
        return classStudentRepository.findByCourseClassId(classId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassStudent> getStudentsByClass(Long classId, String keyword, enums.StudentClassStatus status, Pageable pageable) {
        String normalizedKeyword = normalizeKeyword(keyword);
        return classStudentRepository.searchByClass(classId, normalizedKeyword, status, pageable);
    }

    @Override
    public void addStudentToClass(Long classId, Long studentId) {
        CourseClass courseClass = courseClassRepository.findById(classId).orElseThrow(()
                -> new RuntimeException(" Class Not Found"));

        User student = userRepository.findById(studentId).orElseThrow(()
                -> new RuntimeException(" Student Not Found"));
        if (!hasRole(student, "STUDENT")) {
            throw new RuntimeException("Select user is not a student ");
        }

        boolean exists = classStudentRepository.existsByCourseClassIdAndStudentId(courseClass.getId(), studentId);
        if (exists) {
            throw new RuntimeException("Student already exist  in this class");
        }
        ClassStudent classStudent = new ClassStudent();
        classStudent.setCourseClass(courseClass);
        classStudent.setStudent(student);
        classStudent.setStatus(enums.StudentClassStatus.STUDYING);
        classStudent.setJoinedAt(LocalDate.now());

        classStudentRepository.save(classStudent);

    }

    @Override
    public void updateStudentStatus(Long classStudentId, enums.StudentClassStatus newStatus, String note) {
        ClassStudent classStudent = classStudentRepository.findById(classStudentId).orElseThrow(()
                -> new RuntimeException("Class student not found"));
        enums.StudentClassStatus oldStatus = classStudent.getStatus();
        if (oldStatus == newStatus) {
            throw new RuntimeException("Trang thai moi trung voi trang thai hien tai");
        }
        classStudent.setStatus(newStatus);

        switch (newStatus) {
            case WAITING_TRANSFER, DROPPED, COMPLETED -> classStudent.setLeftAt(LocalDate.now());
            default -> classStudent.setLeftAt(null);
        }
        classStudentRepository.save(classStudent);
        StudentStatusHistory history = new StudentStatusHistory();
        history.setClassStudent(classStudent);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedByUser(securityService.getCurrentUser());
        history.setChangedAt(LocalDateTime.now());
        history.setNote(note);

        studentStatusHistoryRepository.save(history);


    }

    @Override
    public void transferStudent(Long studentId, Long fromClassId, Long toClassId, String note) {
        CourseClass fromClass = courseClassRepository.findById(fromClassId).orElseThrow(()
                -> new ResourceNotFoundException("Source class not found."));

        CourseClass toClass = courseClassRepository.findById(toClassId).orElseThrow(()
                -> new ResourceNotFoundException("Target class not found."));

        ClassStudent oldClassStudent = classStudentRepository.findByCourseClassIdAndStudentId(fromClassId, studentId).orElseThrow(()
                -> new ResourceNotFoundException("Student is not in source class."));

        if (classStudentRepository.existsByCourseClassIdAndStudentId(toClassId, studentId)) {
            throw new BusinessException("Student already exists in target class");
        }
        User currentUser = securityService.getCurrentUser();
        enums.StudentClassStatus oldStatus = oldClassStudent.getStatus();
        oldClassStudent.setStatus(enums.StudentClassStatus.WAITING_TRANSFER);
        oldClassStudent.setLeftAt(LocalDate.now());
        classStudentRepository.save(oldClassStudent);



        StudentStatusHistory history = new StudentStatusHistory();
        history.setClassStudent(oldClassStudent);
        history.setOldStatus(oldStatus);
        history.setNewStatus(enums.StudentClassStatus.WAITING_TRANSFER);
        history.setChangedByUser(currentUser);
        history.setChangedAt(LocalDateTime.now());
        history.setNote(note);
        studentStatusHistoryRepository.save(history);


        ClassStudent newClassStudent = new ClassStudent();
        newClassStudent.setCourseClass(toClass);
        newClassStudent.setStudent(oldClassStudent.getStudent());
        newClassStudent.setStatus(enums.StudentClassStatus.STUDYING);
        newClassStudent.setJoinedAt(LocalDate.now());
        classStudentRepository.save(newClassStudent);


    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseClass> getAllClasses() {
        return courseClassRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseClass> getAllClasses(String keyword) {
        return courseClassRepository.searchByKeyword(normalizeKeyword(keyword));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseClass getClassById(Long classId) {
        return courseClassRepository.findById(classId).orElseThrow(() -> new ResourceNotFoundException("Class not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getStudents() {
        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow(() -> new RuntimeException("Student Role not found"));
        return userRepository.findByRole(studentRole);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getStudents(String keyword, Pageable pageable) {
        return userRepository.searchStudents(normalizeKeyword(keyword), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public User getStudentById(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!hasRole(student, "STUDENT")) {
            throw new BusinessException("Selected user is not a student");
        }
        return student;
    }

    @Override
    @Transactional(readOnly = true)
    public ClassStudent getClassStudentById(Long classStudentId) {
        return classStudentRepository.findById(classStudentId)
                .orElseThrow(() -> new ResourceNotFoundException("Class student not found"));
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null || keyword.trim().isEmpty() ? null : keyword.trim();
    }

    private boolean hasRole(User user, String roleName) {
        if (user.getRole() == null || user.getRole().getName() == null) {
            return false;
        }

        String currentRole = user.getRole().getName();
        return roleName.equals(currentRole) || ("ROLE_" + roleName).equals(currentRole);
    }
}
