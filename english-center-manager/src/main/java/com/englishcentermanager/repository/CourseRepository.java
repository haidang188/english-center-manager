package com.englishcentermanager.repository;

import com.englishcentermanager.entity.Course;
import com.englishcentermanager.entity.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);

    boolean existsByCourseCode(String courseCode);

    boolean existsByCourseCodeAndIdNot(String courseCode, Long id);

    List<Course> findByActiveTrue();

    List<Course> findByCourseType(CourseType courseType);

    List<Course> findByCourseNameContainingIgnoreCase(String keyword);

    List<Course> findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(
            String courseName,
            String courseCode
    );
}