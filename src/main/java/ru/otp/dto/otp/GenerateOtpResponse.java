package ru.otp.dto.otp;

import java.time.Instant;
import java.util.List;

public record GenerateOtpResponse(
        String operationId,
        Instant expiresAt,
        List<DeliveryResultResponse> deliveries,
        String message
) {
}
