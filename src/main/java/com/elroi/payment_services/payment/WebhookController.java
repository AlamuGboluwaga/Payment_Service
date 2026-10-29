package com.elroi.payment_services.payment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.*;
import org.springframework.web.server.ResponseStatusException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;



@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final String secretKey;

    public WebhookController(PaymentService paymentService, ObjectMapper objectMapper,
            @Value("${paystack.secret-key}") String secretKey) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
        this.secretKey = secretKey;
    }

    @PostMapping("/paystack")
    public void receive(
            @RequestHeader(
                    value = "x-paystack-signature",
                    required = false
            ) String signature,
            @RequestBody byte[] rawBody) {

        if (!isValidSignature(rawBody, signature)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid Paystack signature"
            );
        }

        try {
            JsonNode event = objectMapper.readTree(rawBody);

            String eventName = event.path("event").asText();

            if ("charge.success".equals(eventName)) {
                String reference = event.path("data")
                        .path("reference").asText();

                if (reference.isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Missing payment reference"
                    );
                }

                // Verify against Paystack's API before
                // accepting the transaction as successful.
                paymentService.verify(reference);
            }

            // Ignore other event types until implemented.
            // Return 200 after successfully handling them.

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to process webhook"
            );
        }
    }

    private boolean isValidSignature(
            byte[] rawBody, String suppliedSignature) {

        if (suppliedSignature == null
                || suppliedSignature.isBlank()) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA512");

            mac.init(new SecretKeySpec(
                    secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    "HmacSHA512"
            ));

            byte[] expected = HexFormat.of().parseHex(
                    HexFormat.of().formatHex(mac.doFinal(rawBody))
            );

            byte[] supplied =
                    HexFormat.of().parseHex(suppliedSignature);

            return MessageDigest.isEqual(expected, supplied);

        } catch (Exception ex) {
            return false;
        }
    }
}