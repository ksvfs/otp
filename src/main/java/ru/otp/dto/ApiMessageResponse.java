package ru.otp.dto;

import java.time.Instant;

public record ApiMessageResponse(
        Instant timestamp,
        String message
) {
}
