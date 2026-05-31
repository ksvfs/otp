package ru.otp.notification;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.otp.config.AppProperties;
import ru.otp.model.DeliveryChannel;

@Component
public class FileNotificationService implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(FileNotificationService.class);

    private final AppProperties appProperties;

    public FileNotificationService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public DeliveryChannel channel() {
        return DeliveryChannel.FILE;
    }

    @Override
    public void send(String destination, String code, String username, String operationId) {
        Path filePath = Path.of(appProperties.getFile().getPath());
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            String line = String.format(
                    "%s | пользователь=%s | операция=%s | код=%s%n",
                    Instant.now(),
                    username,
                    operationId,
                    code
            );
            Files.writeString(filePath, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            log.info("Код сохранен в файл: путь={}, пользователь={}, операция={}", filePath, username, operationId);
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось сохранить код в файл", exception);
        }
    }
}
