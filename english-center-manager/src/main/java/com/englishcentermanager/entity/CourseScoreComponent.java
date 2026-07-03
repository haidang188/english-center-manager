package com.englishcentermanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "course_score_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseScoreComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 50)
    private String componentCode;

    @Column(nullable = false, length = 100)
    private String componentName;

    private BigDecimal maxScore;

    private BigDecimal weightPercent;

    private Integer displayOrder;

    private Boolean required = true;

    private Boolean calculated = false;
}