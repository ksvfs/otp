package ru.otp.dto.otp;

import java.time.Instant;

public record OtpConfigResponse(
        Integer codeLength,
        Integer ttlSeconds,
        Instant updatedAt
) {
}
