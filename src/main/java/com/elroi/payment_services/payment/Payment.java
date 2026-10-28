package com.elroi.payment_services.payment;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments", uniqueConstraints = {@UniqueConstraint(name = "uk_payment_reference", columnNames = "reference"), @UniqueConstraint(name = "uk_payment_idempotency", columnNames = "idempotency_key")})
@Getter
@Setter
@NoArgsConstructor

public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "NGN";

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    private Long providerTransactionId;

    @Column(length = 1000)
    private String authorizationUrl;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    public Payment(String email, BigDecimal amount, String reference, String idempotencyKey) {
        this.email = email;
        this.amount = amount;
        this.reference = reference;
        this.idempotencyKey = idempotencyKey;
    }
    @PrePersist
    public void beforeInsert() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }
    @PreUpdate
    public void beforeUpdate() {
        updatedAt = Instant.now();
    }

}
