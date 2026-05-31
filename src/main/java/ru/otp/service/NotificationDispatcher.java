package ru.otp.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import ru.otp.model.DeliveryChannel;
import ru.otp.notification.NotificationSender;

@Service
public class NotificationDispatcher {

    private final Map<DeliveryChannel, NotificationSender> senders;

    public NotificationDispatcher(List<NotificationSender> senders) {
        this.senders = new EnumMap<>(DeliveryChannel.class);
        for (NotificationSender sender : senders) {
            this.senders.put(sender.channel(), sender);
        }
    }

    public void send(DeliveryChannel channel, String destination, String code, String username, String operationId) {
        NotificationSender sender = senders.get(channel);
        if (sender == null) {
            throw new IllegalStateException("Канал " + channel + " не поддерживается");
        }
        sender.send(destination, code, username, operationId);
    }
}
