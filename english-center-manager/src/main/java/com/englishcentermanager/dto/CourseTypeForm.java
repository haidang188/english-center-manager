package com.englishcentermanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseTypeForm {
    private Long id;

    @NotBlank(message = "Ma loai khoa hoc khong duoc de trong")
    @Size(max = 50, message = "Ma loai khoa hoc toi da 50 ky tu")
    private String typeCode;

    @NotBlank(message = "Ten loai khoa hoc khong duoc de trong")
    @Size(max = 100, message = "Ten loai khoa hoc toi da 100 ky tu")
    private String typeName;

    @Size(max = 500, message = "Mo ta toi da 500 ky tu")
    private String description;

    private Boolean active = true;
}
