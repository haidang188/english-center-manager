package com.englishcentermanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ExamSessionForm {
    @NotBlank(message = "Vui long nhap ten dot diem")
    private String examName;

    @NotNull(message = "Vui long chon ngay")
    private LocalDate examDate;

    private String note;
}
