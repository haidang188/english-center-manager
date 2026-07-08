package com.englishcentermanager.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class TuitionBatchForm {
    @NotNull(message = "Vui long chon lop")
    private Long classId;

    @NotNull(message = "Vui long nhap so tien")
    @DecimalMin(value = "0.01", message = "So tien phai lon hon 0")
    private BigDecimal amount;

    @NotNull(message = "Vui long chon han nop")
    private LocalDate dueDate;

    private String note;
}
