package ru.otp.dto.otp;

public record DeliveryResultResponse(
        String channel,
        boolean success,
        String message
) {
}
