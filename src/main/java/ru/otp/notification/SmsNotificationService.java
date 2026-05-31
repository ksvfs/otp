package ru.otp.notification;

import java.nio.charset.StandardCharsets;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.otp.config.AppProperties;
import ru.otp.model.DeliveryChannel;

@Component
public class SmsNotificationService implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);

    private final AppProperties appProperties;

    public SmsNotificationService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public DeliveryChannel channel() {
        return DeliveryChannel.SMS;
    }

    @Override
    public void send(String destination, String code, String username, String operationId) {
        SMPPSession session = new SMPPSession();
        try {
            AppProperties.Smpp smpp = appProperties.getSmpp();
            BindParameter bindParameter = new BindParameter(
                    BindType.BIND_TX,
                    smpp.getSystemId(),
                    smpp.getPassword(),
                    smpp.getSystemType(),
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    smpp.getSourceAddr()
            );

            session.connectAndBind(smpp.getHost(), smpp.getPort(), bindParameter);
            session.submitShortMessage(
                    smpp.getSystemType(),
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    smpp.getSourceAddr(),
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    destination,
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    null,
                    null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                    (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT),
                    (byte) 0,
                    ("Код подтверждения для операции " + operationId + ": " + code)
                            .getBytes(StandardCharsets.UTF_8)
            );
            log.info("Код отправлен по SMS: номер={}, пользователь={}, операция={}", destination, username, operationId);
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось отправить код по SMS", exception);
        } finally {
            try {
                if (session.getSessionState().isBound()) {
                    session.unbindAndClose();
                }
            } catch (Exception exception) {
                log.warn("Не удалось корректно закрыть SMPP-сессию: {}", exception.getMessage());
            }
        }
    }
}
