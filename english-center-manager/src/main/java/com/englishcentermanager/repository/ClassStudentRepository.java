package com.englishcentermanager.repository;

import com.englishcentermanager.entity.ClassStudent;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.enums;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassStudentRepository extends JpaRepository<ClassStudent, Long> {

    Page<ClassStudent> findByCourseClassId(Long classId, Pageable pageable);

    @Query("""
            select cs from ClassStudent cs
            where cs.courseClass.id = :classId
            and (:keyword is null
                or lower(cs.student.fullName) like lower(concat('%', :keyword, '%'))
                or lower(cs.student.email) like lower(concat('%', :keyword, '%')))
            and (:status is null or cs.status = :status)
            """)
    Page<ClassStudent> searchByClass(
            @Param("classId") Long classId,
            @Param("keyword") String keyword,
            @Param("status") enums.StudentClassStatus status,
            Pageable pageable
    );

    boolean existsByCourseClassIdAndStudentId(Long classId, Long studentId);

    Optional<ClassStudent> findByCourseClassIdAndStudentId(Long classId, Long studentId);

    Page<ClassStudent> findByCourseClassIdAndStudentFullNameContainingIgnoreCase(
            Long classId,
            String keyword,
            Pageable pageable
    );

    Page<ClassStudent> findByCourseClassIdAndStatus(
            Long classId,
            enums.StudentClassStatus status,
            Pageable pageable
    );

    Page<ClassStudent> findByCourseClassIdAndStudentFullNameContainingIgnoreCaseAndStatus(
            Long classId,
            String keyword,
            enums.StudentClassStatus status,
            Pageable pageable
    );

    long countByCourseClassId(Long classId);

    long countByCourseClass(CourseClass courseClass);

    List<ClassStudent> findByCourseClassIdAndStatus(Long classId, enums.StudentClassStatus status);
}
