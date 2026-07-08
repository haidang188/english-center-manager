package com.englishcentermanager.repository;

import com.englishcentermanager.entity.ClassStudent;
import com.englishcentermanager.entity.CourseScoreComponent;
import com.englishcentermanager.entity.ExamSession;
import com.englishcentermanager.entity.ScoreEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScoreEntryRepository extends JpaRepository<ScoreEntry, Long> {
    @Query("""
            select avg(entry.scoreValue)
            from ScoreEntry entry
            where entry.classStudent.courseClass.id = :classId
            """)
    Double averageScoreByClassId(@Param("classId") Long classId);

    @Query("""
            select avg(entry.scoreValue)
            from ScoreEntry entry
            where entry.classStudent.courseClass.course.id = :courseId
            """)
    Double averageScoreByCourseId(@Param("courseId") Long courseId);

    List<ScoreEntry> findByExamSessionAndClassStudentIn(ExamSession examSession,
                                                        List<ClassStudent> classStudents);

    List<ScoreEntry> findByExamSessionAndClassStudent(ExamSession examSession,
                                                      ClassStudent classStudent);

    Optional<ScoreEntry> findByExamSessionAndClassStudentAndScoreComponent(ExamSession examSession,
                                                                           ClassStudent classStudent,
                                                                           CourseScoreComponent scoreComponent);
}
