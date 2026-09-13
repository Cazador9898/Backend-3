package cl.duoc.bancoxyz.bff.dto;

import java.math.BigDecimal;

public record AtmBalanceResponse(
        Long cuentaId,
        BigDecimal saldoDisponible,
        String estado
) {}
