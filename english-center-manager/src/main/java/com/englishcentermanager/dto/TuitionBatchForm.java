package com.englishcentermanager.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class TuitionBatchForm {

    @NotNull(message = "Vui lòng chọn lớp học")
    private Long classId;

    @NotNull(message = "Vui lòng nhập học phí")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Học phí phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Vui lòng chọn hạn đóng")
    private LocalDate dueDate;

    @Size(max = 255,
            message = "Ghi chú không được vượt quá 255 ký tự")
    private String note;
}
