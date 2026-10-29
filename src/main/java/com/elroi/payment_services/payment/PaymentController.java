package com.elroi.payment_services.payment;


import com.elroi.payment_services.payment.dtos.PaymentRequest;
import com.elroi.payment_services.payment.dtos.PaymentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @GetMapping({"", "/"})
    public String check() {
        return "Payment service is running";
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse initialize(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("Idempotency-Key")
            String idempotencyKey) {

        return service.initialize(request, idempotencyKey);
    }

    @GetMapping("/{reference}")
    public PaymentResponse get(
            @PathVariable String reference) {
        return service.get(reference);
    }

    @GetMapping("/{reference}/verify")
    public PaymentResponse verify(
            @PathVariable String reference) {
        return service.verify(reference);
    }

    @GetMapping("/callback")
    public PaymentResponse callback(
            @RequestParam String reference) {
        return service.verify(reference);
    }
}
