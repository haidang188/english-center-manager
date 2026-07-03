package com.englishcentermanager.service;

import com.englishcentermanager.entity.Course;
import com.englishcentermanager.entity.CourseType;

import java.util.List;
import java.util.Optional;

public interface CourseService {
    List<Course> findAll();

    List<Course> findAllActive();

    Optional<Course> findById(Long id);

    Optional<Course> findByCourseCode(String courseCode);

    Course save(Course course);

    Course update(Long id, Course course);

    void activate(Long id);

    void deactivate(Long id);

    List<Course> findByCourseType(CourseType courseType);

    List<Course> searchByKeyword(String keyword);

    boolean existsByCourseCode(String courseCode);
}
