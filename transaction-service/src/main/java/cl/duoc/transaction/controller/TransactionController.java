package cl.duoc.transaction.controller;

import cl.duoc.transaction.event.TransactionEvent;
import cl.duoc.transaction.event.TransactionEventProducer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    public record Tx(Long id, Long accountId, String fecha, BigDecimal monto, String tipo) {}
    public record CreateTx(Long accountId, BigDecimal monto, String tipo) {}

    private final RestClient client;
    private final TransactionEventProducer producer;
    private final String accountServiceUrl;
    private final AtomicLong sequence = new AtomicLong(3);
    private final List<Tx> data = new CopyOnWriteArrayList<>(List.of(
            new Tx(1L, 101L, "2024-01-01", new BigDecimal("1000"), "debito"),
            new Tx(2L, 101L, "2024-01-02", new BigDecimal("1500"), "credito"),
            new Tx(3L, 102L, "2024-01-03", new BigDecimal("200"), "debito")
    ));

    public TransactionController(RestClient.Builder builder,
                                 TransactionEventProducer producer,
                                 @Value("${services.account.url:http://localhost:8081}") String accountServiceUrl) {
        this.client = builder.build();
        this.producer = producer;
        this.accountServiceUrl = accountServiceUrl;
    }

    @GetMapping
    public List<Tx> all() {
        return data;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tx create(@RequestBody CreateTx request) {
        long id = sequence.incrementAndGet();
        Tx tx = new Tx(id, request.accountId(), LocalDate.now().toString(), request.monto(), request.tipo());
        data.add(tx);
        producer.publish(new TransactionEvent(tx.id(), tx.accountId(), tx.fecha(), tx.monto(), tx.tipo()));
        return tx;
    }

    @GetMapping("/account/{id}/summary")
    @CircuitBreaker(name = "accountService", fallbackMethod = "fallback")
    public Map<String, Object> summary(@PathVariable Long id,
                                       @RequestHeader("Authorization") String authorization) {
        Object account = client.get()
                .uri(accountServiceUrl + "/api/accounts/{id}", id)
                .header("Authorization", authorization)
                .retrieve()
                .body(Object.class);

        List<Tx> accountTransactions = data.stream()
                .filter(tx -> Objects.equals(tx.accountId(), id))
                .toList();

        return Map.of(
                "estado", "OK",
                "cuenta", account,
                "transacciones", accountTransactions
        );
    }

    public Map<String, Object> fallback(Long id, String authorization, Throwable ex) {
        List<Tx> accountTransactions = data.stream()
                .filter(tx -> Objects.equals(tx.accountId(), id))
                .toList();

        return Map.of(
                "estado", "DEGRADADO",
                "cuentaId", id,
                "mensaje", "Account Service no disponible; Resilience4j activa respuesta de respaldo",
                "transacciones", accountTransactions
        );
    }
}
