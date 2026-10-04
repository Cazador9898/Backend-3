package cl.duoc.account.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventConsumer {
    @KafkaListener(topics = "transaction.created", groupId = "account-service-group")
    public void consume(TransactionEvent event) {
        System.out.println("[KAFKA] Evento procesado por account-service: " + event);
    }
}
