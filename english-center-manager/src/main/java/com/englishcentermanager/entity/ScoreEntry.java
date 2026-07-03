package com.englishcentermanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "score_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"exam_session_id", "class_student_id", "score_component_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoreEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exam_session_id", nullable = false)
    private ExamSession examSession;

    @ManyToOne
    @JoinColumn(name = "class_student_id", nullable = false)
    private ClassStudent classStudent;

    @ManyToOne
    @JoinColumn(name = "score_component_id", nullable = false)
    private CourseScoreComponent scoreComponent;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal scoreValue;

    @ManyToOne
    @JoinColumn(name = "created_by_teacher_id", nullable = false)
    private User createdByTeacher;

    @ManyToOne
    @JoinColumn(name = "updated_by_teacher_id")
    private User updatedByTeacher;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
