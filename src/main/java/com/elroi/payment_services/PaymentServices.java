package com.elroi.payment_services;


import com.elroi.payment_services.payment.GblobalExceptionHandler.DuplicateIdempotencyKey;
import com.elroi.payment_services.payment.GblobalExceptionHandler.InvalidAmountException;
import com.elroi.payment_services.payment.Payment;
import com.elroi.payment_services.payment.PaymentRepository;
import com.elroi.payment_services.payment.PaymentStatus;
import com.elroi.payment_services.payment.dtos.PaymentRequest;
import com.elroi.payment_services.payment.dtos.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
public class PaymentServices {

    private final PaymentRepository repository;


    public PaymentServices(PaymentRepository repository) {
        this.repository = repository;
    }

    public PaymentResponse initialize(PaymentRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 100) {
            throw new RuntimeException("Valid Idempotence key is required");
        }

        var transactionExists = repository.findByIdempotencyKey(idempotencyKey);
        if (transactionExists.isPresent()) {
            var transaction = transactionExists.get();

            if (!transaction.getEmail().equalsIgnoreCase(request.getEmail()) || transaction.getAmount().compareTo(request.getAmount()) != 0) {
                log.info("Response {}", "This Idempotency Key has already been used with another transaction details");
                throw new DataIntegrityViolationException("message : This Idempotency Key has already been used with another transaction details");
            } else {
                throw new DuplicateIdempotencyKey("message : This transaction already exists");
            }
        }
        try {
            BigDecimal amount = request.getAmount().setScale(2, RoundingMode.UNNECESSARY);
            String reference = "PAY-" + UUID.randomUUID().toString().replace("-", "");
            PaymentStatus status = PaymentStatus.PENDING;

//            Payment payment = new Payment(request.getEmail(), amount, reference, idempotencyKey);
            Payment payment = Payment.builder().email(request.getEmail()).amount(amount).currency("NGN").reference(reference).idempotencyKey(idempotencyKey).status(status).build();
            Payment savedepayment = repository.saveAndFlush(payment);
            return PaymentResponse.from(savedepayment);
        } catch (ArithmeticException e) {
            throw new InvalidAmountException("message : Transaction amount must have at most 2 decimal places");
        }


    }


}
