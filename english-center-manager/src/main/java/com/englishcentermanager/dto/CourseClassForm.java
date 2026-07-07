package com.englishcentermanager.dto;

import com.englishcentermanager.entity.enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CourseClassForm {
    private Long id;

    @NotBlank(message = "Ma lop khong duoc de trong")
    @Size(max = 30, message = "Ma lop toi da 30 ky tu")
    private String classCode;

    @NotBlank(message = "Ten lop khong duoc de trong")
    @Size(max = 100, message = "Ten lop toi da 100 ky tu")
    private String className;

    @NotNull(message = "Vui long chon khoa hoc")
    private Long courseId;

    @NotNull(message = "Vui long chon giao vien")
    private Long teacherId;

    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Vui long chon trang thai")
    private enums.ClassStatus status = enums.ClassStatus.PLANNED;
}
