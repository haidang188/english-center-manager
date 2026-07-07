package com.englishcentermanager.repository;

import com.englishcentermanager.entity.ClassStudent;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassStudentRepository extends JpaRepository<ClassStudent, Long> {
    long countByCourseClass(CourseClass courseClass);

    long countByCourseClassAndStatus(CourseClass courseClass, enums.StudentClassStatus status);

    List<ClassStudent> findByCourseClass(CourseClass courseClass);

    List<ClassStudent> findByStudent(User student);
}
