package ru.otp.dto.otp;

import jakarta.validation.constraints.NotBlank;

public record ValidateOtpRequest(
        @NotBlank(message = "Идентификатор операции обязателен")
        String operationId,

        @NotBlank(message = "Код обязателен")
        String code
) {
}
