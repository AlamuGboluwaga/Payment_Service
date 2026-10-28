package com.elroi.payment_services.payment;


import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

@Component
public class PaystackClient {

    private final RestClient client;

    public PaystackClient(RestClient paystackRestClient) {
        this.client = paystackRestClient;
    }

    public JsonNode initialize(
            String email,
            long amountKobo,
            String reference,
            String callbackUrl) {

        var body = new java.util.HashMap<String, Object>();
        body.put("email", email);
        body.put("amount", amountKobo);
        body.put("currency", "NGN");
        body.put("reference", reference);
        body.put("callback_url", callbackUrl);

        JsonNode response = client.post()
                .uri("/transaction/initialize")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        if (response == null
                || !response.path("status").asBoolean()) {
            throw new IllegalStateException(
                    "Paystack initialization was unsuccessful"
            );
        }

        return response.path("data");
    }

    public JsonNode verify(String reference) {

        JsonNode response = client.get()
                .uri("/transaction/verify/{reference}", reference)
                .retrieve()
                .body(JsonNode.class);

        if (response == null
                || !response.path("status").asBoolean()) {
            throw new IllegalStateException(
                    "Paystack verification failed"
            );
        }

        return response.path("data");
    }
}
