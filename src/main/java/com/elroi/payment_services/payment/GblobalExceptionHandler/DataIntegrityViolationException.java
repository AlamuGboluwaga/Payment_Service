package com.elroi.payment_services.payment.GblobalExceptionHandler;

public class DataIntegrityViolationException extends RuntimeException {
    public DataIntegrityViolationException(String message) {
        super(message);
    }
}
