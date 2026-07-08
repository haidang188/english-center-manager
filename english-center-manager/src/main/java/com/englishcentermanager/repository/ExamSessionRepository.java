package com.englishcentermanager.repository;

import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    List<ExamSession> findByCourseClassOrderByExamDateDescIdDesc(CourseClass courseClass);

    Optional<ExamSession> findFirstByCourseClassOrderByExamDateDescIdDesc(CourseClass courseClass);
}
