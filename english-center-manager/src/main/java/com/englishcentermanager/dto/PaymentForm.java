package com.englishcentermanager.dto;

import com.englishcentermanager.entity.enums;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentForm {

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal paidAmount;

    @NotNull
    private enums.PaymentMethod paymentMethod;

    private String note;

}
