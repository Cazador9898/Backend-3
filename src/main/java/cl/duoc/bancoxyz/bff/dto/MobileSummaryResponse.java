package cl.duoc.bancoxyz.bff.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MobileSummaryResponse(
        Long cuentaId,
        String nombre,
        BigDecimal saldo,
        String tipoCuenta,
        BigDecimal saldoNetoAnual,
        List<MovimientoLigero> ultimosMovimientos
) {
    public record MovimientoLigero(
            LocalDate fecha,
            String tipo,
            BigDecimal monto
    ) {}
}
