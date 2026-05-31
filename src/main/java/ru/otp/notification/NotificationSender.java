package ru.otp.notification;

import ru.otp.model.DeliveryChannel;

public interface NotificationSender {

    DeliveryChannel channel();

    void send(String destination, String code, String username, String operationId);
}
