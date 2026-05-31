package ru.otp.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.otp.service.OtpService;

@Component
public class OtpExpirationScheduler {

    private final OtpService otpService;

    public OtpExpirationScheduler(OtpService otpService) {
        this.otpService = otpService;
    }

    @Scheduled(fixedDelayString = "${app.otp.expire-check-interval-ms:60000}")
    public void expireCodes() {
        otpService.expireOutdatedCodes();
    }
}
