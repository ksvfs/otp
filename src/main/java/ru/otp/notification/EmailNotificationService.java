package ru.otp.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import ru.otp.config.AppProperties;
import ru.otp.model.DeliveryChannel;

@Component
public class EmailNotificationService implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    public EmailNotificationService(JavaMailSender mailSender, AppProperties appProperties) {
        this.mailSender = mailSender;
        this.appProperties = appProperties;
    }

    @Override
    public DeliveryChannel channel() {
        return DeliveryChannel.EMAIL;
    }

    @Override
    public void send(String destination, String code, String username, String operationId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(appProperties.getMail().getFrom());
        message.setTo(destination);
        message.setSubject("Код подтверждения операции");
        message.setText("Пользователь: " + username + "\nОперация: " + operationId + "\nВаш код подтверждения: " + code);
        mailSender.send(message);
        log.info("Код отправлен по email: адрес={}, пользователь={}, операция={}", destination, username, operationId);
    }
}
