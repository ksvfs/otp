package ru.otp.dto.admin;

import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        String role,
        String email,
        String phone,
        String telegramChatId,
        Instant createdAt
) {
}
