package ru.otp.dto.otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record GenerateOtpRequest(
        @NotBlank(message = "Идентификатор операции обязателен")
        String operationId,

        @NotEmpty(message = "Нужно указать хотя бы один канал отправки")
        Set<String> channels,

        String email,
        String phone,
        String telegramChatId
) {
}
