package com.englishcentermanager.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CourseScoreComponentForm {
    private Long id;

    @NotBlank(message = "Ma thanh phan diem khong duoc de trong")
    @Size(max = 50, message = "Ma thanh phan diem toi da 50 ky tu")
    private String componentCode;

    @NotBlank(message = "Ten thanh phan diem khong duoc de trong")
    @Size(max = 100, message = "Ten thanh phan diem toi da 100 ky tu")
    private String componentName;

    @NotNull(message = "Diem toi da khong duoc de trong")
    @DecimalMin(value = "0.0", inclusive = false, message = "Diem toi da phai lon hon 0")
    private BigDecimal maxScore;

    @NotNull(message = "Ty trong diem khong duoc de trong")
    @DecimalMin(value = "0.0", message = "Ty trong khong duoc am")
    @DecimalMax(value = "100.0", message = "Ty trong khong duoc vuot qua 100")
    private BigDecimal weightPercent;

    @NotNull(message = "Thu tu hien thi khong duoc de trong")
    private Integer displayOrder;

    private Boolean required = true;

    private Boolean calculated = false;
}
