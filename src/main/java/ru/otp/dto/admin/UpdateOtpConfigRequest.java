package ru.otp.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateOtpConfigRequest(
        @NotNull(message = "Длина кода обязательна")
        @Min(value = 4, message = "Длина кода должна быть не меньше 4")
        @Max(value = 10, message = "Длина кода должна быть не больше 10")
        Integer codeLength,

        @NotNull(message = "Время жизни кода обязательно")
        @Min(value = 30, message = "Время жизни должно быть не меньше 30 секунд")
        @Max(value = 3600, message = "Время жизни должно быть не больше 3600 секунд")
        Integer ttlSeconds
) {
}
