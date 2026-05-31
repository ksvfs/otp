package ru.otp.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otp.dto.otp.GenerateOtpRequest;
import ru.otp.dto.otp.GenerateOtpResponse;
import ru.otp.dto.otp.ValidateOtpRequest;
import ru.otp.dto.otp.ValidateOtpResponse;
import ru.otp.exception.UnauthorizedException;
import ru.otp.security.AuthenticatedUser;
import ru.otp.service.OtpService;

@RestController
@RequestMapping("/api/user/otp")
public class UserOtpController {

    private final OtpService otpService;

    public UserOtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/generate")
    public GenerateOtpResponse generate(@Valid @RequestBody GenerateOtpRequest request, Authentication authentication) {
        return otpService.generate(currentUser(authentication), request);
    }

    @PostMapping("/validate")
    public ValidateOtpResponse validate(@Valid @RequestBody ValidateOtpRequest request, Authentication authentication) {
        return otpService.validate(currentUser(authentication), request);
    }

    private AuthenticatedUser currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new UnauthorizedException("Не удалось определить текущего пользователя");
        }
        return user;
    }
}
