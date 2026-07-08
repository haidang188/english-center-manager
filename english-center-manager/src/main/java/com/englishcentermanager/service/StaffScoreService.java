package com.englishcentermanager.service;

import com.englishcentermanager.dto.ExamSessionForm;
import com.englishcentermanager.dto.StaffScoreBoard;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.ExamSession;
import com.englishcentermanager.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface StaffScoreService {
    List<ExamSession> findSessions(CourseClass courseClass);

    Optional<ExamSession> findLatestSession(CourseClass courseClass);

    ExamSession findOrCreateDefaultSession(Long classId, User createdBy);

    ExamSession createSession(Long classId, ExamSessionForm form, User createdBy);

    StaffScoreBoard buildScoreBoard(Long classId, Long examSessionId);

    void saveScores(Long classId, Long examSessionId, Map<String, String> params, User currentUser);
}
