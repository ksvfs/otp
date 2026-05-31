package ru.otp.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otp.config.AppProperties;
import ru.otp.dao.OtpCodeDao;
import ru.otp.dao.OtpConfigDao;
import ru.otp.dao.UserDao;
import ru.otp.dto.otp.DeliveryResultResponse;
import ru.otp.dto.otp.GenerateOtpRequest;
import ru.otp.dto.otp.GenerateOtpResponse;
import ru.otp.dto.otp.ValidateOtpRequest;
import ru.otp.dto.otp.ValidateOtpResponse;
import ru.otp.exception.BadRequestException;
import ru.otp.exception.NotFoundException;
import ru.otp.model.DeliveryChannel;
import ru.otp.model.OtpCode;
import ru.otp.model.OtpStatus;
import ru.otp.model.User;
import ru.otp.security.AuthenticatedUser;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OtpConfigDao otpConfigDao;
    private final OtpCodeDao otpCodeDao;
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final NotificationDispatcher notificationDispatcher;
    private final AppProperties appProperties;

    public OtpService(OtpConfigDao otpConfigDao,
                      OtpCodeDao otpCodeDao,
                      UserDao userDao,
                      PasswordEncoder passwordEncoder,
                      NotificationDispatcher notificationDispatcher,
                      AppProperties appProperties) {
        this.otpConfigDao = otpConfigDao;
        this.otpCodeDao = otpCodeDao;
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.notificationDispatcher = notificationDispatcher;
        this.appProperties = appProperties;
    }

    @Transactional
    public GenerateOtpResponse generate(AuthenticatedUser currentUser, GenerateOtpRequest request) {
        User user = userDao.findById(currentUser.userId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<DeliveryChannel> channels = parseChannels(request.channels());
        validateDestinations(user, request, channels);

        var config = otpConfigDao.getCurrent();
        String code = generateCode(config.codeLength());
        Instant expiresAt = Instant.now().plusSeconds(config.ttlSeconds());

        otpCodeDao.expireActiveByUserAndOperation(user.id(), request.operationId());
        OtpCode otpCode = otpCodeDao.create(
                user.id(),
                request.operationId(),
                passwordEncoder.encode(code),
                channels,
                expiresAt
        );

        List<DeliveryResultResponse> deliveries = new ArrayList<>();
        for (DeliveryChannel channel : channels) {
            String destination = resolveDestination(user, request, channel);
            try {
                notificationDispatcher.send(channel, destination, code, user.username(), request.operationId());
                deliveries.add(new DeliveryResultResponse(channel.name(), true, "Код успешно отправлен"));
            } catch (Exception exception) {
                log.error("Ошибка отправки OTP: пользователь={}, операция={}, канал={}", user.username(), request.operationId(), channel, exception);
                deliveries.add(new DeliveryResultResponse(channel.name(), false, exception.getMessage()));
            }
        }

        long successCount = deliveries.stream().filter(DeliveryResultResponse::success).count();
        String message = successCount == 0
                ? "Код сгенерирован, но не был доставлен ни по одному каналу"
                : "Код успешно сгенерирован";
        log.info("OTP создан: id={}, пользователь={}, операция={}, expiresAt={}, успешныхОтправок={}",
                otpCode.id(), user.username(), request.operationId(), expiresAt, successCount);

        return new GenerateOtpResponse(request.operationId(), expiresAt, deliveries, message);
    }

    @Transactional
    public ValidateOtpResponse validate(AuthenticatedUser currentUser, ValidateOtpRequest request) {
        OtpCode otpCode = otpCodeDao.findLatestActive(currentUser.userId(), request.operationId())
                .orElse(null);

        if (otpCode == null) {
            log.info("Не найден активный OTP: пользователь={}, операция={}", currentUser.username(), request.operationId());
            return new ValidateOtpResponse(false, OtpStatus.EXPIRED.name(), "Активный код не найден");
        }

        if (otpCode.expiresAt().isBefore(Instant.now())) {
            otpCodeDao.markExpired(otpCode.id());
            log.info("OTP просрочен при валидации: id={}, пользователь={}, операция={}", otpCode.id(), currentUser.username(), request.operationId());
            return new ValidateOtpResponse(false, OtpStatus.EXPIRED.name(), "Срок действия кода истек");
        }

        if (!passwordEncoder.matches(request.code(), otpCode.codeHash())) {
            log.info("Передан неверный OTP: id={}, пользователь={}, операция={}", otpCode.id(), currentUser.username(), request.operationId());
            return new ValidateOtpResponse(false, otpCode.status().name(), "Неверный код подтверждения");
        }

        otpCodeDao.markUsed(otpCode.id(), Instant.now());
        log.info("OTP успешно подтвержден: id={}, пользователь={}, операция={}", otpCode.id(), currentUser.username(), request.operationId());
        return new ValidateOtpResponse(true, OtpStatus.USED.name(), "Код подтвержден успешно");
    }

    @Transactional
    public int expireOutdatedCodes() {
        int updated = otpCodeDao.expireOlderThan(Instant.now());
        if (updated > 0) {
            log.info("Помечены просроченные OTP-коды: {}", updated);
        }
        return updated;
    }

    private List<DeliveryChannel> parseChannels(Set<String> channels) {
        try {
            return channels.stream()
                    .map(value -> DeliveryChannel.valueOf(value.trim().toUpperCase(Locale.ROOT)))
                    .distinct()
                    .sorted(Comparator.comparing(Enum::name))
                    .toList();
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Указан неподдерживаемый канал отправки");
        }
    }

    private void validateDestinations(User user, GenerateOtpRequest request, List<DeliveryChannel> channels) {
        for (DeliveryChannel channel : channels) {
            if (channel == DeliveryChannel.FILE) {
                continue;
            }
            String destination = resolveDestination(user, request, channel);
            if (destination == null || destination.isBlank()) {
                throw new BadRequestException("Для канала " + channel + " не указан получатель");
            }
        }
    }

    private String resolveDestination(User user, GenerateOtpRequest request, DeliveryChannel channel) {
        return switch (channel) {
            case EMAIL -> firstNonBlank(request.email(), user.email());
            case SMS -> firstNonBlank(request.phone(), user.phone());
            case TELEGRAM -> firstNonBlank(request.telegramChatId(), user.telegramChatId(), appProperties.getTelegram().getDefaultChatId());
            case FILE -> "";
        };
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String generateCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(SECURE_RANDOM.nextInt(10));
        }
        return builder.toString();
    }
}
