package com.englishcentermanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, length = 20)
    private String phoneNumber;
    @Column(nullable = false, length = 100)
    private String fullName;
    private LocalDate dateOfBirth;
    private String address;
    @Column(unique = true, length = 30)
    private String identityNumber;
    @Enumerated(EnumType.STRING)
    private enums.UserStatus status = enums.UserStatus.ACTIVE;
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
