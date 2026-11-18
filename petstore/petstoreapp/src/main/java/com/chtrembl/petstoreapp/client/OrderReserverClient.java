
package com.chtrembl.petstoreapp.client;

import com.chtrembl.petstoreapp.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderReserverClient {

    @Value("${petstore.order-reserver.url}")
    private String functionUrl;

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    public void reserveOrder(Order order, String sessionId) throws Exception {

        String json = objectMapper.writeValueAsString(order);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(functionUrl))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("X-Session-ID", sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "Azure Function failed. HTTP status: "
                            + response.statusCode()
                            + ", response: " + response.body()
            );
        }

        log.info("Order successfully sent to Azure Function for session: {}",
                sessionId);
    }
}
