package ru.otp.notification;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.otp.config.AppProperties;
import ru.otp.model.DeliveryChannel;

@Component
public class TelegramNotificationService implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(TelegramNotificationService.class);

    private final AppProperties appProperties;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public TelegramNotificationService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public DeliveryChannel channel() {
        return DeliveryChannel.TELEGRAM;
    }

    @Override
    public void send(String destination, String code, String username, String operationId) {
        String botToken = appProperties.getTelegram().getBotToken();
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException("Токен Telegram-бота не задан");
        }

        String message = String.format(
                "%s, код подтверждения для операции %s: %s",
                username,
                operationId,
                code
        );
        String url = String.format(
                "%s/bot%s/sendMessage?chat_id=%s&text=%s",
                appProperties.getTelegram().getBaseUrl(),
                botToken,
                destination,
                encode(message)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Telegram API вернул статус " + response.statusCode());
            }
            log.info("Код отправлен в Telegram: chatId={}, пользователь={}, операция={}", destination, username, operationId);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Отправка в Telegram была прервана", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось отправить код в Telegram", exception);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
