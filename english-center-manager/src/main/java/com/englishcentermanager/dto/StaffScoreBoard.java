package com.englishcentermanager.dto;

import com.englishcentermanager.entity.ClassStudent;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.CourseScoreComponent;
import com.englishcentermanager.entity.ExamSession;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class StaffScoreBoard {
    private CourseClass courseClass;
    private ExamSession examSession;
    private List<ClassStudent> classStudents;
    private List<CourseScoreComponent> inputComponents;
    private List<CourseScoreComponent> calculatedComponents;
    private Map<String, BigDecimal> scoreValues = new HashMap<>();
    private Map<Long, BigDecimal> averageScores = new HashMap<>();

    public BigDecimal scoreValue(Long classStudentId, Long componentId) {
        return scoreValues.get(classStudentId + "_" + componentId);
    }

    public BigDecimal averageScore(Long classStudentId) {
        return averageScores.get(classStudentId);
    }
}
