package com.englishcentermanager.repository;

import com.englishcentermanager.entity.StudentTuition;
import com.englishcentermanager.entity.TuitionBatch;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentTuitionRepository extends JpaRepository<StudentTuition, Long> {

    List<StudentTuition> findByTuitionBatch(TuitionBatch tuitionBatch);

    Page<StudentTuition> findByTuitionBatch(TuitionBatch tuitionBatch, Pageable pageable);

    Optional<StudentTuition> findByTuitionBatchAndStudent(TuitionBatch tuitionBatch, User student);

    List<StudentTuition> findByStudentOrderByCreatedAtDesc(User student);

    @Query("""
    SELECT st
    FROM StudentTuition st
    WHERE st.tuitionBatch.id = :batchId
    AND (
        :keyword IS NULL
        OR LOWER(st.student.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(st.student.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
    """)
    Page<StudentTuition> search(
            @Param("batchId") Long batchId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
    @Query("""
            select studentTuition
            from StudentTuition studentTuition
            where studentTuition.tuitionBatch = :tuitionBatch
            order by studentTuition.student.fullName asc
            """)
    List<StudentTuition> findByTuitionBatchOrderByStudentName(@Param("tuitionBatch") TuitionBatch tuitionBatch);

    long countByTuitionBatchAndStatus(TuitionBatch tuitionBatch, enums.TuitionStatus status);

    long countByTuitionBatch(TuitionBatch tuitionBatch);

    long countByStatus(enums.TuitionStatus status);
}
