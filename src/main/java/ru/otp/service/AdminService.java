package ru.otp.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otp.dao.OtpConfigDao;
import ru.otp.dao.UserDao;
import ru.otp.dto.admin.UpdateOtpConfigRequest;
import ru.otp.dto.admin.UserResponse;
import ru.otp.dto.otp.OtpConfigResponse;
import ru.otp.exception.BadRequestException;
import ru.otp.exception.NotFoundException;
import ru.otp.model.Role;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final OtpConfigDao otpConfigDao;
    private final UserDao userDao;

    public AdminService(OtpConfigDao otpConfigDao, UserDao userDao) {
        this.otpConfigDao = otpConfigDao;
        this.userDao = userDao;
    }

    public OtpConfigResponse getConfig() {
        var config = otpConfigDao.getCurrent();
        return new OtpConfigResponse(config.codeLength(), config.ttlSeconds(), config.updatedAt());
    }

    @Transactional
    public OtpConfigResponse updateConfig(UpdateOtpConfigRequest request) {
        var updated = otpConfigDao.update(request.codeLength(), request.ttlSeconds());
        log.info("Конфигурация OTP обновлена: длина={}, ttlSeconds={}", updated.codeLength(), updated.ttlSeconds());
        return new OtpConfigResponse(updated.codeLength(), updated.ttlSeconds(), updated.updatedAt());
    }

    public List<UserResponse> getUsers() {
        return userDao.findAllNonAdmins().stream()
                .map(user -> new UserResponse(
                        user.id(),
                        user.username(),
                        user.role().name(),
                        user.email(),
                        user.phone(),
                        user.telegramChatId(),
                        user.createdAt()
                ))
                .toList();
    }

    @Transactional
    public void deleteUser(Long userId) {
        var user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (user.role() == Role.ADMIN) {
            throw new BadRequestException("Удаление администратора запрещено");
        }

        userDao.deleteById(userId);
        log.info("Пользователь удален: id={}, логин={}", user.id(), user.username());
    }
}
