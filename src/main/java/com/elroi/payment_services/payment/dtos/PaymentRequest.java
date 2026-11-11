package com.elroi.payment_services.payment.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter

public class PaymentRequest {
    @NotNull
//    @Email
    private String email;
    @NotNull
    @DecimalMin(value = "1.00", inclusive = true, message = "Amount must be greater than zero")
    @Digits(integer = 15, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

}
