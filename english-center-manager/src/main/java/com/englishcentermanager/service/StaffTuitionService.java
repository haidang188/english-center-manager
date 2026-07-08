package com.englishcentermanager.service;

import com.englishcentermanager.dto.TuitionBatchForm;
import com.englishcentermanager.entity.StudentTuition;
import com.englishcentermanager.entity.TuitionBatch;
import com.englishcentermanager.entity.User;

import java.util.List;
import java.util.Optional;

public interface StaffTuitionService {
    List<TuitionBatch> findBatches(Long classId);

    Optional<TuitionBatch> findById(Long id);

    TuitionBatch createBatch(TuitionBatchForm form, User createdBy);

    List<StudentTuition> findStudentTuitions(TuitionBatch tuitionBatch);

    long countStudentTuitions(TuitionBatch tuitionBatch);
}
