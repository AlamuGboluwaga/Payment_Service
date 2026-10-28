package com.elroi.payment_services.payment.dtos;

import com.elroi.payment_services.payment.Payment;
import com.elroi.payment_services.payment.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResponse(String reference, BigDecimal amount, String currency, PaymentStatus status, String authorizationUrl)
{
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getReference(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getAuthorizationUrl()
        );
    }
}