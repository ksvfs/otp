package ru.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otp.dao.UserDao;
import ru.otp.dto.ApiMessageResponse;
import ru.otp.dto.auth.AuthResponse;
import ru.otp.dto.auth.LoginRequest;
import ru.otp.dto.auth.RegisterRequest;
import ru.otp.exception.BadRequestException;
import ru.otp.exception.UnauthorizedException;
import ru.otp.model.Role;
import ru.otp.model.User;
import ru.otp.security.JwtService;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserDao userDao, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public ApiMessageResponse register(RegisterRequest request) {
        if (userDao.findByUsername(request.username()).isPresent()) {
            throw new BadRequestException("Пользователь с таким логином уже существует");
        }

        Role role = Role.valueOf(request.role());
        if (role == Role.ADMIN && userDao.adminExists()) {
            throw new BadRequestException("В системе уже зарегистрирован администратор");
        }

        try {
            User createdUser = userDao.create(
                    request.username(),
                    passwordEncoder.encode(request.password()),
                    role,
                    normalize(request.email()),
                    normalize(request.phone()),
                    normalize(request.telegramChatId())
            );
            log.info("Пользователь зарегистрирован: id={}, логин={}, роль={}", createdUser.id(), createdUser.username(), createdUser.role());
            return new ApiMessageResponse(java.time.Instant.now(), "Пользователь успешно зарегистрирован");
        } catch (DataIntegrityViolationException exception) {
            throw new BadRequestException("Не удалось зарегистрировать пользователя");
        }
    }

    public AuthResponse login(LoginRequest request) {
        User user = userDao.findByUsername(request.username())
                .orElseThrow(() -> new UnauthorizedException("Неверный логин или пароль"));

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new UnauthorizedException("Неверный логин или пароль");
        }

        log.info("Успешный вход в систему: id={}, логин={}, роль={}", user.id(), user.username(), user.role());
        return jwtService.generateToken(user);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
