package com.englishcentermanager.repository;

import com.englishcentermanager.entity.ClassStudent;
import com.englishcentermanager.entity.StudentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentStatusHistoryRepository extends JpaRepository<StudentStatusHistory, Long> {
    List<StudentStatusHistory> findByClassStudentOrderByChangedAtDesc(ClassStudent classStudent);
}
