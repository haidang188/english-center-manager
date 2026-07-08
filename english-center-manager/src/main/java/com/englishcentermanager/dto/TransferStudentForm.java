package com.englishcentermanager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferStudentForm {
    @NotNull
    private Long studentId;
    @NotNull
    private Long fromClassId;
    @NotNull
    private Long toClassId;

    private String note;
}
