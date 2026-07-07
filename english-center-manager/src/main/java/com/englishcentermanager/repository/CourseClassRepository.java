package com.englishcentermanager.repository;

import com.englishcentermanager.entity.Course;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseClassRepository extends JpaRepository<CourseClass, Long> {
    Optional<CourseClass> findByClassCode(String classCode);

    boolean existsByClassCode(String classCode);

    boolean existsByClassCodeAndIdNot(String classCode, Long id);

    List<CourseClass> findByCourse(Course course);

    List<CourseClass> findByTeacher(User teacher);

    List<CourseClass> findByStatus(enums.ClassStatus status);

    List<CourseClass> findByClassNameContainingIgnoreCase(String keyword);
}
