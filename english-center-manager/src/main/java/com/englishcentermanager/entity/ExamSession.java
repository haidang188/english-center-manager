package com.englishcentermanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private CourseClass courseClass;

    @Column(nullable = false, length = 100)
    private String examName;

    @Column(nullable = false)
    private LocalDate examDate;

    private String note;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
