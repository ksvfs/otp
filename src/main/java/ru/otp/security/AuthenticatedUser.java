package ru.otp.security;

import ru.otp.model.Role;

public record AuthenticatedUser(
        Long userId,
        String username,
        Role role
) {
}
