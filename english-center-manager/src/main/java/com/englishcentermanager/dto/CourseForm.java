package com.englishcentermanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseForm {
    private Long id;
    @NotBlank(message = "Mã khóa học không được để trống")
    @Size(max = 30,message = "Mã khóa học tối đa 30 ký tự")
    private String courseCode;
    @NotBlank(message = "Tên khóa học không được để trống")
    @Size(max = 100, message = "Tên khóa học tối đa 100 ký tự")
    private String courseName;
    @NotNull(message = "Vui long chọn loại khóa học")
    private Long courseTypeId;
    @Size(max = 50, message = "Mô tả tối đa 500 kí tự")
    private String description;
    private Boolean active = true;
}
