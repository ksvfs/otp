package ru.otp.model;

import java.time.Instant;

public record OtpConfig(
        Integer id,
        Integer codeLength,
        Integer ttlSeconds,
        Instant updatedAt
) {
}
