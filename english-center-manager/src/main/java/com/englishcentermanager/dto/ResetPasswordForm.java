package com.englishcentermanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordForm {
    @NotBlank(message = "Vui long nhap mat khau moi")
    @Size(min = 6, message = "Mat khau phai co it nhat 6 ky tu")
    private String newPassword;

    @NotBlank(message = "Vui long xac nhan mat khau")
    private String confirmPassword;
}
