package com.englishcentermanager.repository;

import com.englishcentermanager.entity.StudentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentStatusHistoryRepository extends JpaRepository<StudentStatusHistory, Long> {

}
