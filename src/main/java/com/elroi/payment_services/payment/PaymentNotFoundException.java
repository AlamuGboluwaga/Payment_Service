package com.elroi.payment_services.payment;


public class PaymentNotFoundException
        extends RuntimeException {

    public PaymentNotFoundException(String reference) {
        super("Payment not found: " + reference);
    }
}