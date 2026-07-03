package com.englishcentermanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_tuition_id", nullable = false)
    private StudentTuition studentTuition;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private enums.PaymentMethod paymentMethod;

    private String note;
}
