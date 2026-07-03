package com.englishcentermanager.repository;

import com.englishcentermanager.entity.Course;
import com.englishcentermanager.entity.CourseScoreComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseScoreComponentRepository extends JpaRepository<CourseScoreComponent, Long> {
    Optional<CourseScoreComponent> findByCourseAndComponentCode(Course course, String componentCode);

    boolean existsByCourseAndComponentCode(Course course, String componentCode);

    List<CourseScoreComponent> findByCourse(Course course);

    List<CourseScoreComponent> findByCourseOrderByDisplayOrderAsc(Course course);

    List<CourseScoreComponent> findByComponentNameContainingIgnoreCase(String keyword);
}
