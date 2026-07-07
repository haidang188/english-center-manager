package com.englishcentermanager.dto;

import com.englishcentermanager.entity.enums;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class ClassScheduleForm {
    private Long id;

    @NotNull(message = "Vui long chon lop hoc")
    private Long classId;

    @NotNull(message = "Vui long chon phong hoc")
    private Long roomId;

    @NotNull(message = "Vui long chon thu")
    private enums.DayOfWeek dayOfWeek;

    @NotNull(message = "Vui long chon gio bat dau")
    private LocalTime startTime;

    @NotNull(message = "Vui long chon gio ket thuc")
    private LocalTime endTime;

    @Size(max = 255, message = "Ghi chu toi da 255 ky tu")
    private String note;
}
