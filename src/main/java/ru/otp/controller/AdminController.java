package ru.otp.controller;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otp.dto.ApiMessageResponse;
import ru.otp.dto.admin.UpdateOtpConfigRequest;
import ru.otp.dto.admin.UserResponse;
import ru.otp.dto.otp.OtpConfigResponse;
import ru.otp.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/config")
    public OtpConfigResponse getConfig() {
        return adminService.getConfig();
    }

    @PutMapping("/config")
    public OtpConfigResponse updateConfig(@Valid @RequestBody UpdateOtpConfigRequest request) {
        return adminService.updateConfig(request);
    }

    @GetMapping("/users")
    public List<UserResponse> getUsers() {
        return adminService.getUsers();
    }

    @DeleteMapping("/users/{userId}")
    public ApiMessageResponse deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return new ApiMessageResponse(Instant.now(), "Пользователь и связанные OTP-коды удалены");
    }
}
