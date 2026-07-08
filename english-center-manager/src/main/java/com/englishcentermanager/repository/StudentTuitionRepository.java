package com.englishcentermanager.repository;

import com.englishcentermanager.entity.StudentTuition;
import com.englishcentermanager.entity.TuitionBatch;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentTuitionRepository extends JpaRepository<StudentTuition, Long> {
    @Query("""
            select studentTuition
            from StudentTuition studentTuition
            where studentTuition.tuitionBatch = :tuitionBatch
            order by studentTuition.student.fullName asc
            """)
    List<StudentTuition> findByTuitionBatchOrderByStudentName(@Param("tuitionBatch") TuitionBatch tuitionBatch);

    List<StudentTuition> findByStudentOrderByCreatedAtDesc(User student);

    boolean existsByTuitionBatchAndStudent(TuitionBatch tuitionBatch, User student);

    long countByTuitionBatch(TuitionBatch tuitionBatch);

    long countByStatus(enums.TuitionStatus status);
}
