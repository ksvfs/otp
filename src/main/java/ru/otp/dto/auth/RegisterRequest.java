package ru.otp.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Логин обязателен")
        @Size(min = 3, max = 100, message = "Логин должен содержать от 3 до 100 символов")
        String username,

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 6, max = 100, message = "Пароль должен содержать от 6 до 100 символов")
        String password,

        @NotBlank(message = "Роль обязательна")
        @Pattern(regexp = "ADMIN|USER", message = "Роль должна быть ADMIN или USER")
        String role,

        String email,
        String phone,
        String telegramChatId
) {
}
