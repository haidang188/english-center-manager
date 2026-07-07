package com.englishcentermanager.dto;

import com.englishcentermanager.entity.enums;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserForm {
    private Long id;

    @NotBlank(message = "Vui long nhap ho ten")
    private String fullName;

    @NotBlank(message = "Vui long nhap email")
    @Email(message = "Email khong hop le")
    private String email;

    @Pattern(regexp = "^$|.{6,}", message = "Mat khau phai co it nhat 6 ky tu")
    private String password;

    @NotBlank(message = "Vui long nhap so dien thoai")
    private String phoneNumber;

    private LocalDate dateOfBirth;

    private String address;

    private String identityNumber;

    @NotNull(message = "Vui long chon vai tro")
    private Long roleId;

    @NotNull(message = "Vui long chon trang thai")
    private enums.UserStatus status;
}
