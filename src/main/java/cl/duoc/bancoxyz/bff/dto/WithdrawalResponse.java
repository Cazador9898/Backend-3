package cl.duoc.bancoxyz.bff.dto;

import java.math.BigDecimal;

public record WithdrawalResponse(
        Long cuentaId,
        String atmId,
        BigDecimal montoRetirado,
        BigDecimal saldoRestante,
        String mensaje
) {}
