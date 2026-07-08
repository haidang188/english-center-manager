package com.englishcentermanager.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomForm {
    private Long id;

    @NotBlank(message = "Vui lòng nhập mã phòng.")
    @Size(max = 30, message = "Mã phòng không được vượt quá 30 ký tự.")
    private String roomCode;

    @NotBlank(message = "Vui lòng nhập tên phòng.")
    @Size(max = 100, message = "Tên phòng không được vượt quá 100 ký tự.")
    private String roomName;

    @NotNull(message = "Vui lòng nhập sức chứa.")
    @Min(value = 1, message = "Sức chứa phải lớn hơn 0.")
    @Max(value = 100, message = "Sức chứa không nên vượt quá 100.")
    private Integer capacity;

    private Boolean active = true;
}
