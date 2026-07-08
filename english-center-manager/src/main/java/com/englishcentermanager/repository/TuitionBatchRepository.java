package com.englishcentermanager.repository;

import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.TuitionBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TuitionBatchRepository extends JpaRepository<TuitionBatch, Long> {
    List<TuitionBatch> findAllByOrderByCreatedAtDesc();

    List<TuitionBatch> findByCourseClassOrderByCreatedAtDesc(CourseClass courseClass);
}
