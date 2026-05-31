package ru.otp.dto.otp;

public record ValidateOtpResponse(
        boolean valid,
        String status,
        String message
) {
}
