package com.englishcentermanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "course_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String typeCode;
    @Column(nullable = false, length = 100)
    private String typeName;
    private String description;
    private Boolean active = true;
}
