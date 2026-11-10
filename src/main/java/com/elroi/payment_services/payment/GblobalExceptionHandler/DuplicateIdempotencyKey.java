package com.elroi.payment_services.payment.GblobalExceptionHandler;

public class DuplicateIdempotencyKey extends RuntimeException {
    public DuplicateIdempotencyKey(String message) {
        super(message);
    }
}
