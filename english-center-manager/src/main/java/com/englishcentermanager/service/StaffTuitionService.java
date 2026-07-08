package com.englishcentermanager.service;

import com.englishcentermanager.dto.PaymentForm;
import com.englishcentermanager.dto.TuitionBatchForm;
import com.englishcentermanager.entity.PaymentHistory;
import com.englishcentermanager.entity.StudentTuition;
import com.englishcentermanager.entity.TuitionBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StaffTuitionService {

    Page<TuitionBatch> getTuitionBatches(String keyword, Pageable pageable);

    TuitionBatch createBatch(TuitionBatchForm form);

    TuitionBatch getBatch(Long id);

    Page<StudentTuition> getStudentTuitions(Long batchId, String keyword, Pageable pageable);

    void updatePayment(Long studentTuitionId, PaymentForm form);

    List<PaymentHistory> getPaymentHistory(Long studentTuitionId);

    StudentTuition getStudentTuition(Long id);

}
