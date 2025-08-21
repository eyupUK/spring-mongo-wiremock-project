package com.practice.mongoapi.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Component
public class PricingClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PricingClient(RestTemplate restTemplate,
                         @Value("${pricing.base-url:http://localhost:9090}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public double fetchDiscountPct(String title, String author, double price) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/discount")
                .queryParam("title", title)
                .queryParam("author", author)
                .queryParam("price", price)
                .build(true).toUri();
        ResponseEntity<Map> resp = restTemplate.getForEntity(uri, Map.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null || !resp.getBody().containsKey("discountPct")) {
            throw new IllegalStateException("Bad response from pricing service");
        }
        Object val = resp.getBody().get("discountPct");
        if (!(val instanceof Number)) throw new IllegalStateException("discountPct not numeric");
        return ((Number) val).doubleValue();
    }
}
