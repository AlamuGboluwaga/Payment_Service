package com.elroi.payment_services.payment.GblobalExceptionHandler;

public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
