package com.englishcentermanager.dto;

import com.englishcentermanager.entity.enums;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStudentStatusForm {
    @NotNull
    private Long classStudentId;

    @NotNull
    private enums.StudentClassStatus status;
    private String note;
}
