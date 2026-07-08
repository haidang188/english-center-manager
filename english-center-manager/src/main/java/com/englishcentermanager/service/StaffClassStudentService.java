package com.englishcentermanager.service;

import com.englishcentermanager.entity.ClassStudent;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StaffClassStudentService {
    Page<ClassStudent> getStudentsByClass(Long classId, Pageable pageable);

    Page<ClassStudent> getStudentsByClass(Long classId, String keyword, enums.StudentClassStatus status, Pageable pageable);

    void addStudentToClass(Long classId, Long studentId);

    void updateStudentStatus(Long classStudentId, enums.StudentClassStatus newStatus, String note);

    void transferStudent(Long studentId, Long fromClassId, Long toClassId, String note);

    List<CourseClass> getAllClasses();

    List<CourseClass> getAllClasses(String keyword);

    CourseClass getClassById(Long classId);

    List<User> getStudents();

    Page<User> getStudents(String keyword, Pageable pageable);

    User getStudentById(Long studentId);

    ClassStudent getClassStudentById(Long classStudentId);
}
