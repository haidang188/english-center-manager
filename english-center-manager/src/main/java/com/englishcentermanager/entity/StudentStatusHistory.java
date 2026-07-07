package com.englishcentermanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_status_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "class_student_id", nullable = false)
    private ClassStudent classStudent;

    @Enumerated(EnumType.STRING)
    private enums.StudentClassStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private enums.StudentClassStatus newStatus;

    @ManyToOne
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedByUser;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    private String note;
}
