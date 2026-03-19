package com.um.springbootprojstructure.auth.gateway;

public interface NotificationGatewayClient {
    void sendOtp(String phoneNumber, String message);
}

