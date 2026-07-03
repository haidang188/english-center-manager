package com.englishcentermanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "class_students",
        uniqueConstraints = @UniqueConstraint(columnNames = {"class_id", "student_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private CourseClass courseClass;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private enums.StudentClassStatus status = enums.StudentClassStatus.STUDYING;

    @Column(nullable = false)
    private LocalDate joinedAt;

    private LocalDate leftAt;
}
