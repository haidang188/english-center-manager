package com.englishcentermanager.dto;

import com.englishcentermanager.entity.enums;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddStudentForm {

    @NotNull(message = "Vui lòng chọn lớp")
    private Long classId;

    @NotNull(message = "Vui lòng chọn học viên")
    private Long studentId;


}
