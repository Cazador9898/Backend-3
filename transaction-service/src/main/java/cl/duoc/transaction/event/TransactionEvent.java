package cl.duoc.transaction.event;

import java.math.BigDecimal;

public record TransactionEvent(Long id, Long accountId, String fecha, BigDecimal monto, String tipo) {}
