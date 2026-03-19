package com.um.springbootprojstructure.auth.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestNotificationGatewayClient implements NotificationGatewayClient {
    private static final Logger log = LoggerFactory.getLogger(RestNotificationGatewayClient.class);

    private final RestClient restClient;
    private final boolean enabled;

    public RestNotificationGatewayClient(
            RestClient.Builder builder,
            @Value("${mfa.gateway.base-url:}") String baseUrl,
            @Value("${mfa.gateway.enabled:false}") boolean enabled
    ) {
        this.enabled = enabled && baseUrl != null && !baseUrl.isBlank();
        this.restClient = builder.baseUrl(baseUrl == null ? "" : baseUrl.trim()).build();
    }

    @Override
    public void sendOtp(String phoneNumber, String message) {
        if (!enabled) {
            log.info("MFA gateway disabled; would send OTP to {} message={}", phoneNumber, message);
            return;
        }

        restClient.post()
                .uri("/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SendRequest(phoneNumber, message))
                .retrieve()
                .toBodilessEntity();
    }

    record SendRequest(String to, String message) {}
}

