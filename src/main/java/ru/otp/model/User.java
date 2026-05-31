package ru.otp.model;

import java.time.Instant;

public record User(
        Long id,
        String username,
        String passwordHash,
        Role role,
        String email,
        String phone,
        String telegramChatId,
        Instant createdAt
) {
}
