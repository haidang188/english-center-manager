package com.englishcentermanager.service;

import com.englishcentermanager.entity.ClassStudent;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;

import java.time.LocalDate;
import java.util.List;

public interface StaffClassStudentService {
    List<ClassStudent> searchInClass(Long classId, String keyword, enums.StudentClassStatus status);

    ClassStudent addStudentToClass(Long classId, Long studentId, LocalDate joinedAt, User changedBy);

    void updateStatus(Long classStudentId, enums.StudentClassStatus status, User changedBy, String note);

    ClassStudent transferStudent(Long classStudentId, Long targetClassId, User changedBy, String note);
}
