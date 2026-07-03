package com.englishcentermanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String courseCode;

    @Column(nullable = false, length = 100)
    private String courseName;

    @ManyToOne
    @JoinColumn(name = "course_type_id", nullable = false)
    private CourseType courseType;

    private String description;

    private Boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
