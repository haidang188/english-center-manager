package com.englishcentermanager.repository;

import com.englishcentermanager.entity.ClassStudent;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassStudentRepository extends JpaRepository<ClassStudent, Long> {
    long countByCourseClass(CourseClass courseClass);

    long countByCourseClassAndStatus(CourseClass courseClass, enums.StudentClassStatus status);

    List<ClassStudent> findByCourseClass(CourseClass courseClass);

    List<ClassStudent> findByCourseClassOrderByStudentFullNameAsc(CourseClass courseClass);

    List<ClassStudent> findByStudent(User student);

    Optional<ClassStudent> findByCourseClassAndStudent(CourseClass courseClass, User student);

    boolean existsByCourseClassAndStudent(CourseClass courseClass, User student);

    @Query("""
            select classStudent
            from ClassStudent classStudent
            where classStudent.courseClass.id = :classId
              and (:status is null or classStudent.status = :status)
              and (:keyword is null
                   or lower(classStudent.student.fullName) like lower(concat('%', :keyword, '%'))
                   or lower(classStudent.student.email) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(classStudent.student.identityNumber, '')) like lower(concat('%', :keyword, '%')))
            order by classStudent.student.fullName asc
            """)
    List<ClassStudent> searchInClass(@Param("classId") Long classId,
                                     @Param("keyword") String keyword,
                                     @Param("status") enums.StudentClassStatus status);
}
