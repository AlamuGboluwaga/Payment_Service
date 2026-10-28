package com.elroi.payment_services.payment.dtos;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class PaymentRequest {
@NotNull
@Email
    private String email;
@NotNull
@DecimalMin(value = "1.00", inclusive = true, message = "Amount must be greater than zero")
@Digits(integer = 15, fraction = 2)
    private BigDecimal amount;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
