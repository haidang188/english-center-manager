package com.englishcentermanager.repository;

import com.englishcentermanager.entity.PaymentHistory;
import com.englishcentermanager.entity.StudentTuition;
import com.englishcentermanager.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    List<PaymentHistory> findByStudentTuitionOrderByPaidAtDesc(StudentTuition studentTuition);

    @Query("""
            select history
            from PaymentHistory history
            where history.studentTuition.student = :student
            order by history.paidAt desc
            """)
    List<PaymentHistory> findByStudentOrderByPaidAtDesc(@Param("student") User student);
}
