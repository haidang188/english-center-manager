package com.englishcentermanager.service;

import com.englishcentermanager.entity.Course;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;

import java.util.List;
import java.util.Optional;

public interface CourseClassService {
    List<CourseClass> findAll();

    Optional<CourseClass> findById(Long id);

    Optional<CourseClass> findByClassCode(String classCode);

    CourseClass save(CourseClass courseClass);

    CourseClass update(Long id, CourseClass courseClass);

    void updateStatus(Long id, enums.ClassStatus status);

    List<CourseClass> findByCourse(Course course);

    List<CourseClass> findByTeacher(User teacher);

    List<CourseClass> findByStatus(enums.ClassStatus status);

    List<CourseClass> searchByKeyword(String keyword);

    boolean existsByClassCode(String classCode);
}
