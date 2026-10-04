package cl.duoc.transaction.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionEventProducer {
    public static final String TOPIC = "transaction.created";
    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public TransactionEventProducer(KafkaTemplate<String, TransactionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(TransactionEvent event) {
        kafkaTemplate.send(TOPIC, String.valueOf(event.accountId()), event);
    }
}
