package com.englishcentermanager.repository;

import com.englishcentermanager.entity.Course;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseClassRepository extends JpaRepository<CourseClass, Long> {
    Optional<CourseClass> findByClassCode(String classCode);

    boolean existsByClassCode(String classCode);

    List<CourseClass> findByCourse(Course course);

    List<CourseClass> findByTeacher(User teacher);

    List<CourseClass> findByStatus(enums.ClassStatus status);

    List<CourseClass> findByClassNameContainingIgnoreCase(String keyword);

    @Query("""
            select c from CourseClass c
            where (:keyword is null
                or lower(c.className) like lower(concat('%', :keyword, '%'))
                or lower(c.classCode) like lower(concat('%', :keyword, '%')))
            """)
    List<CourseClass> searchByKeyword(@Param("keyword") String keyword);
}
