package com.englishcentermanager.repository;

import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.TuitionBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TuitionBatchRepository extends JpaRepository<TuitionBatch, Long> {

    Page<TuitionBatch> findAll(Pageable pageable);

    List<TuitionBatch> findByCourseClass(CourseClass courseClass);

    @Query("""
        SELECT t
        FROM TuitionBatch t
        WHERE (:keyword IS NULL
            OR LOWER(t.courseClass.className)
                LIKE LOWER(CONCAT('%',:keyword,'%'))
            OR LOWER(t.courseClass.classCode)
                LIKE LOWER(CONCAT('%',:keyword,'%'))
            OR LOWER(t.note)
                LIKE LOWER(CONCAT('%',:keyword,'%')))
        """)
    Page<TuitionBatch> search(
            @Param("keyword") String keyword,
            Pageable pageable);

}
