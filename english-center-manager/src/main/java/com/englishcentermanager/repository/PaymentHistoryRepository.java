package com.englishcentermanager.repository;

import com.englishcentermanager.entity.PaymentHistory;
import com.englishcentermanager.entity.StudentTuition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    List<PaymentHistory> findByStudentTuitionOrderByPaidAtDesc(StudentTuition studentTuition);

}