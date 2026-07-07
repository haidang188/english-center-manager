package com.englishcentermanager.service;

import com.englishcentermanager.entity.Course;
import com.englishcentermanager.entity.CourseScoreComponent;

import java.util.List;
import java.util.Optional;

public interface CourseScoreComponentService {
    List<CourseScoreComponent> findAll();

    Optional<CourseScoreComponent> findById(Long id);

    CourseScoreComponent save(CourseScoreComponent courseScoreComponent);

    CourseScoreComponent update(Long id, CourseScoreComponent courseScoreComponent);

    void deleteById(Long id);

    List<CourseScoreComponent> findByCourse(Course course);

    List<CourseScoreComponent> findByCourseOrderByDisplayOrder(Course course);

    List<CourseScoreComponent> searchByKeyword(String keyword);

    boolean existsByCourseAndComponentCode(Course course, String componentCode);
}
