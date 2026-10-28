package com.elroi.payment_services.payment;


import com.elroi.payment_services.payment.dtos.PaymentRequest;
import com.elroi.payment_services.payment.dtos.PaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final PaystackClient paystack;
    private final String callbackUrl;

    public PaymentService(
            PaymentRepository repository,
            PaystackClient paystack,
            org.springframework.core.env.Environment env) {
        this.repository = repository;
        this.paystack = paystack;
        this.callbackUrl = env.getRequiredProperty(
                "paystack.callback-url"
        );
    }

    public PaymentResponse initialize(
            PaymentRequest request,
            String idempotencyKey) {

        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "A valid Idempotency-Key header is required"
            );
        }

        var existing = repository
                .findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            Payment payment = existing.get();

            if (!payment.getEmail().equalsIgnoreCase(
                    request.getEmail())
                    || payment.getAmount().compareTo(
                    request.getAmount()) != 0) {
                throw new IllegalArgumentException(
                        "Idempotency key was already used " +
                                "with different payment details"
                );
            }

            return PaymentResponse.from(payment);
        }

        BigDecimal amount = request.getAmount()
                .setScale(2, RoundingMode.UNNECESSARY);

        String reference = "PAY-" +
                UUID.randomUUID().toString().replace("-", "");

        Payment payment = new Payment(
                request.getEmail(),
                amount,
                reference,
                idempotencyKey
        );

        // Persist before making the external API call.
        payment = repository.saveAndFlush(payment);

        long amountKobo = amount
                .movePointRight(2)
                .longValueExact();

        JsonNode data = paystack.initialize(
                payment.getEmail(),
                amountKobo,
                payment.getReference(),
                callbackUrl
        );

        payment.setAuthorizationUrl(
                data.path("authorization_url").asText()
        );

        return PaymentResponse.from(repository.save(payment));
    }

    public PaymentResponse verify(String reference) {

        Payment payment = repository.findByReference(reference)
                .orElseThrow(() ->
                        new PaymentNotFoundException(reference));

        // A confirmed success should never be downgraded.
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return PaymentResponse.from(payment);
        }

        JsonNode data = paystack.verify(reference);

        String verifiedReference =
                data.path("reference").asText();

        String verifiedStatus =
                data.path("status").asText();

        long verifiedAmount =
                data.path("amount").asLong(-1);

        String currency =
                data.path("currency").asText();

        long expectedAmount = payment.getAmount()
                .movePointRight(2)
                .longValueExact();

        // Never trust status alone.
        if (!payment.getReference().equals(verifiedReference)
                || verifiedAmount != expectedAmount
                || !payment.getCurrency().equals(currency)) {
            throw new IllegalStateException(
                    "Verified payment details do not match"
            );
        }

        if ("success".equalsIgnoreCase(verifiedStatus)) {
            payment.setStatus(PaymentStatus.SUCCESS);

            if (data.path("id").canConvertToLong()) {
                payment.setProviderTransactionId(
                        data.path("id").longValue()
                );
            }
        } else if ("failed".equalsIgnoreCase(verifiedStatus)
                || "abandoned".equalsIgnoreCase(verifiedStatus)) {
            payment.setStatus(PaymentStatus.FAILED);
        }
        // pending, ongoing, processing and other uncertain
        // states remain PENDING.

        return PaymentResponse.from(repository.save(payment));
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(String reference) {
        return PaymentResponse.from(
                repository.findByReference(reference)
                        .orElseThrow(() ->
                                new PaymentNotFoundException(reference))
        );
    }
}
