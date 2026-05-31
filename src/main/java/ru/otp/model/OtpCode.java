package ru.otp.model;

import java.time.Instant;
import java.util.List;

public record OtpCode(
        Long id,
        Long userId,
        String operationId,
        String codeHash,
        OtpStatus status,
        List<DeliveryChannel> deliveryChannels,
        Instant expiresAt,
        Instant createdAt,
        Instant usedAt
) {
}
